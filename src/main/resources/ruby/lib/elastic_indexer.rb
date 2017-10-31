require 'logger'
require 'elasticsearch'

class ElasticIndexer
  def initialize(client: nil,autocommit: 0, indexname: "cloudtrail", typename: "type", logger: nil)
    @records = []
    @autocommit_threshold = autocommit
    @indexname = indexname
    @typename = typename
    @logger = logger if(logger!=nil) else Logger.new(STDOUT)
    if client
      @client=client
    else
      @client=Elasticsearch::Client.new()
    end
  end #def initialize

  def flatten_hash(h)
    newhash={}
    h.each do |k,v|
      if v.is_a?(Hash)
        v.each {|subkey,subval|
          if subval.is_a?(Hash)
            newhash[subkey]=flatten_hash(subval)
          else
            newhash[subkey]=subval
          end
        }
        #h.delete(k)
      else
        newhash[k]=v
      end
    end
    return h
  end #def flatten.hash

  def add_record(rec)
    #$logger.info("adding record")
    if rec.is_a?(Hash)
      @records << self.flatten_hash(rec)
    else
      @records << rec
    end
    if @records.length > @autocommit_threshold
      self.commit
    end
  end #def add_record

  def commit
    $logger.info("Committing to index #{@indexname}...")

    actions = @records.map do |rec|
      {
          index: {
              _index: @indexname,
              _type: @typename,
              data: rec
          }
      }
    end

    @client.bulk(body: actions)
    @records = []
  end #def commit

end #class ElasticIndexer