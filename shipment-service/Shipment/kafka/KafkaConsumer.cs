using System;
using Confluent.Kafka;
using Microsoft.Extensions.Configuration;

namespace Shipment.kafka
{
    public class KafkaConsumer
    {
        private ConsumerConfig _config = null;
        private KafkaProducer _producer = null;
        private string _publisherTopic = string.Empty;
         public KafkaConsumer(IConfiguration Configuration , KafkaProducer producer)
        {
            var endpoint = (string)Configuration.GetSection("Kafka").GetValue(typeof(string), "endpoint");
            var consumerGroupName = (string)Configuration.GetSection("Kafka").GetValue(typeof(string), "consumerGroup");
            _publisherTopic = (string)Configuration.GetSection("Kafka").GetValue(typeof(string), "publisherTopic");


            _config = new ConsumerConfig
            {
                BootstrapServers = endpoint,
                GroupId = consumerGroupName,
                AutoOffsetReset = AutoOffsetReset.Earliest
            };
            _producer = producer;

        }

        public void Consume(string topicName)
        {
            using (var consumer = new ConsumerBuilder<Ignore, string>(_config).Build())
            {
                consumer.Subscribe(topicName);

                while (true)
                {
                    var consumeResult = consumer.Consume();
                    var message = consumeResult.Message.Value;
                    Console.WriteLine($"Message Recieved : {message}");

                    // handle consumed message.
                    _producer.PublishMessage(message, _publisherTopic);

                }

                consumer.Close();
            }
        }


    }
}

