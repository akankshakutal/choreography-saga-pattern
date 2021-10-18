using System;
using System.Net;
using Confluent.Kafka;
using Microsoft.Extensions.Configuration;

namespace Shipment.kafka
{
    public class KafkaProducer
    {
        private ProducerConfig _config = null;
        public KafkaProducer(IConfiguration Configuration)
        {

           var endpoint = (string)Configuration.GetSection("Kafka").GetValue(typeof(string),"endpoint");
           _config = new ProducerConfig
            {
              
                BootstrapServers = endpoint,
                ClientId = Dns.GetHostName()

            };

        }

        public void PublishMessage(string message, string topicName)
        {
            using (var producer = new ProducerBuilder<Null, string>(_config).Build())
            {
                producer.Produce(topicName, new Message<Null, string> { Value = message});
            }
        }
    }
}

