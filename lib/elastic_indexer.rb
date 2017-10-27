class ElasticIndexer
  def initialize(client: nil,autocommit: 0)
    @records = []
    @autocommit_threshold = autocommit
    if client
      @client=client
    else
      @client=Elasticsearch::Client.new()
    end
  end #def initialize

  def flatten_hash(h)
    #print "flatten_hash got:"
    #ap(h)
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
    #print "flatten_hash returned:"
    #ap(h)
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
    actions = []
    $logger.info("Committing to index #{INDEXNAME}...")
    @records.each do |rec|
      actions << { index: {
          _index: INDEXNAME,
          _type: TYPENAME,
          data: rec
      }}
    end
    @client.bulk(body: actions)
    @records = []
  end #def commit

end #class ElasticIndexer