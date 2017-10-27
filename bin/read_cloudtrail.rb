#!/usr/bin/env ruby

require 'elasticsearch'
require 'json'
require 'awesome_print'
require 'date'
require 'trollop'
require 'logger'
require 'aws-sdk-resources'
require 'zlib'
require 'lib/elastic_indexer'

INDEXNAME='cloudtrail'
TYPENAME="event"
$logger=Logger.new(STDOUT)


def find_numerics(rec)
  rtn=rec
  rec.each {|k,v|
    if v.is_a?(Hash)
      rtn[k] = find_numerics(v)
    else
      begin
        rtn[k] = int(rec[k])
      rescue
        begin
          rtn[k] = float(rec[k])
        rescue
        end
      end
    end
  }
  return rtn
end

def reformat_event(rec)
  rtn=rec
  if rec['eventTime']
    rtn['eventTime'] = DateTime.rfc3339(rec['eventTime'])
  end

  rtn
  #rtn = find_numerics(rec)
end

def download_from_s3(bucket: nil,key: nil)
  if bucket==nil
    raise ArgumentError, "You must supply a bucket name to download_from_s3"
  end
  if key==nil
    raise ArgumentError, "You must specify a key (aka, filepath) to download_from_s3"
  end
  
  $logger.debug("Attempting to download #{key} from #{bucket}...")
  s3 = Aws::S3::Client.new(region: $opts.region)
  b = Aws::S3::Bucket.new(bucket, client: s3)
  if not b.exists?
    raise ArgumentError,"The bucket #{bucket} does not exist."
  end
  
  if not b.object(key).exists?
    $logger.info("#{key} does not exist in #{bucket}, so assuming it's url-encoded")
    key = URI.unescape(key)
    #raise ArgumentError,"The object #{key} does not exist in the bucket #{bucket}"
  end
  
  #download to memory
  b.object(key).get().body
end

def download_and_process(b,object_key, indexer: nil)
  n=0
  compressed_data = download_from_s3(bucket: b, key: object_key)
  
  uncompressed_data = Zlib::GzipReader.new(compressed_data).read
  final_data = JSON.parse(uncompressed_data)
  
  final_data['Records'].each {|rec|
    processed_rec = reformat_event(rec)
    indexer.add_record(processed_rec)
    ap(processed_rec)
    n+=1
  }
  n
end

#START MAIN
$opts = Trollop::options do
  opt :elasticsearch, "Location of elasticsearch cluster to communicate with. Specify multiple hosts separated by commas.", :type=>:string, :default=>"localhost"
  opt :queueurl, "URL of the Amazon SQS queue to listen to", :type=>:string
  opt :region, "AWS region to operate in", :type=>:string, :default=>"eu-west-1"
  opt :reindex, "Perform a complete re-index based on the s3 bucket listed", :type=>:boolean
  opt :bucket, "S3 bucket to re-index from, if performing a re-index", :type=>:string
end

ets = Elasticsearch::Client.new(hosts: $opts.elasticsearch.split(/,\s*/),log: true)
ets.cluster.health
indexer = ElasticIndexer.new(client: ets,autocommit: 500)

if $opts[:reindex]
  $logger.info("Attempting to perform a full re-index from #{$opts.bucket}")
  s3 = Aws::S3::Client.new(region: $opts.region)
  b = Aws::S3::Bucket.new($opts.bucket, client: s3)
  if not b.exists?
    raise ArgumentError,"The bucket #{bucket} does not exist."
  end
  
  n=0
  b.objects.each {|obj|
    begin
      n+=download_and_process(obj.bucket_name,obj.key,indexer: indexer)
    rescue StandardError=>e
      $logger.warn(e)
    end
  }
  $logger.info("Re-index complete, re-indexed #{n} records")
end

if $opts[:queueurl]==nil
  $logger.error("You need to specify a queue to listen to using --queueurl")
  exit(1)
end


