# Security Policy

## Supported Versions

The following versions of Aether Framework are currently supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please report it to us as soon as possible.

### How to Report

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them via email to:

📧 **Security Team**: contact@example.com

Please include the following information in your report:

- A description of the vulnerability
- Steps to reproduce the issue
- Possible impact of the vulnerability
- Any suggested fixes or mitigations

### Response Process

1. **Acknowledgment**: We will acknowledge receipt of your report within 48 hours
2. **Investigation**: We will investigate the issue and determine its impact
3. **Fix**: We will work on a fix and aim to release it within 30 days
4. **Disclosure**: We will disclose the vulnerability after a fix is released

## Security Best Practices

When using Aether Framework, we recommend the following security practices:

### Actor Isolation

- Each actor should have minimal privileges
- Avoid sharing mutable state between actors
- Use immutable messages (Java Records)

### Message Validation

- Validate all incoming messages before processing
- Sanitize message content
- Implement rate limiting for message processing

### Supervision

- Use appropriate supervision strategies
- Monitor actor failure rates
- Implement circuit breakers for external calls

### Dependency Management

- Keep dependencies up to date
- Use `mvn dependency:check` to identify known vulnerabilities
- Subscribe to security advisories for Spring Boot and other dependencies

## Known Security Considerations

### Virtual Threads

- Virtual threads share the same platform thread pool
- Avoid blocking operations that could exhaust the pool
- Monitor thread pinning events (Java 24+)

### Message Serialization

- Default serializer uses Java Serialization
- For production, consider using JSON or Protocol Buffers
- Validate deserialized messages

### Mailbox Capacity

- Unbounded mailboxes can cause memory issues
- Use bounded mailboxes with appropriate capacity
- Monitor mailbox sizes

## Security Updates

Security updates will be released as patch versions (e.g., 1.0.1) and announced via:

- GitHub Releases
- Security Advisories
- CHANGELOG.md

## Acknowledgments

We thank the following individuals and organizations for their responsible disclosure of security issues:

- [Your name here]

## License

This security policy is provided under the same license as the Aether Framework project.
