# URL Shortener Service

**URL Shortener Service** is a microservice designed to shorten long URLs, enabling users to share compact links on social networks and other platforms. Built using Java and Spring Boot, this service showcases a microservices architecture with robust caching and asynchronous processing.

## Overview

- **URL Shortening:**  
  The service accepts a long URL and generates a short URL using a Base62 encoding algorithm. Unique numbers are sourced from a PostgreSQL database.

- **Caching Mechanisms:**  
  Two levels of caching are implemented:
  - **Redis Cache:** Stores recently created and popular URLs for quick retrieval.
  - **Local Cache:** A thread-safe, in-memory structure that holds pre-generated unique hashes, reducing database calls and improving performance.

- **Asynchronous Hash Generation:**  
  The local cache (`HashCache`) is replenished asynchronously. When the cache falls below a set threshold, it automatically retrieves and stores a new batch of hashes from the database.

- **Cleaner Scheduler:**  
  A scheduled job periodically removes outdated URLs from the database and recycles their hashes back into the available pool.

## Project Structure

The main components of the service include:

- **REST Controller (`URLController`):**  
  Handles HTTP requests for URL shortening and redirection. It validates incoming data and interacts with the service layer.

- **URLService:**  
  Implements the core business logic:
  - Generates short URLs.
  - Persists URL associations in PostgreSQL and Redis.
  - Manages the retrieval of unique hashes from the local cache.

- **LocalCache / HashCache:**  
  A thread-safe in-memory cache for storing pre-generated hashes with asynchronous replenishment to ensure rapid response times.

- **HashGenerator:**  
  Converts unique numeric identifiers from PostgreSQL into Base62-encoded hashes, ensuring the uniqueness of short URLs.

- **CleanerScheduler:**  
  Runs periodic jobs to delete outdated URLs and recycle their hashes, maintaining an efficient pool of available hashes.

- **Repositories:**  
  - `URLRepository` for saving and retrieving URL mappings.
  - `URLCacheRepository` for managing popular URLs in Redis.
  - `UniqueIdRepository` and `HashRepository` for generating and storing unique IDs and hashes.

## Workflow

1. **URL Shortening:**
   - The user sends a POST request with a long URL.
   - `URLController` validates the input and passes it to `URLService`.
   - `URLService` retrieves a unique hash from the local cache, saves the URL-hash mapping in PostgreSQL and Redis, and returns the short URL to the user.

2. **Retrieving the Original URL:**
   - When a short URL is accessed, the service first checks Redis for the original URL.
   - If not found, it falls back to querying PostgreSQL.
   - The user is redirected to the original long URL via a 302 HTTP response.

3. **Recycling Hashes:**
   - The `CleanerScheduler` periodically removes old URLs from the database.
   - The associated hashes are returned to the available pool, ensuring they can be reused while maintaining uniqueness.

## Technologies Used

- **Java** with **Spring Boot**
- **PostgreSQL** for persistent storage
- **Redis** for caching popular URLs
