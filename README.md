# Gallery Image Service

Handles user image manipulation logic. Manages metadata (**Postgres**) and binary storage via **Amazon S3** (using **LocalStack** for local dev). Implements **S3 Signed URLs**, **Caffeine** L1 cache, and Spring Retry. Publishes activity events to Kafka topics