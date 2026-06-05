# Contributing to Aether

Thank you for your interest in contributing to Aether! This document provides guidelines for contributing to the project.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/your-username/aether.git`
3. Build the project: `mvn clean install`
4. Run tests: `mvn test`

## Development Setup

### Requirements
- Java 21+ (for virtual threads)
- Maven 3.8+
- Git

### IDE Configuration
- Import as Maven project
- Enable annotation processing (for Spring Boot)
- Set Java compliance level to 21

## Making Changes

### Branch Naming
- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `docs/description` - Documentation updates

### Commit Messages
Follow conventional commits:
```
feat: add new feature
fix: fix bug
docs: update documentation
refactor: refactor code
test: add tests
chore: update build process
```

### Code Style
- Follow existing code style
- Use meaningful variable names
- Add Javadoc for public APIs
- Keep methods focused and small

## Testing

### Running Tests
```bash
# All tests
mvn test

# Specific module
mvn test -pl aether-core

# Specific test class
mvn test -pl aether-core -Dtest=ActorTest
```

### Test Coverage
- Aim for >80% coverage
- Test edge cases
- Test error conditions
- Add integration tests for new features

## Pull Request Process

1. Update documentation
2. Add tests for new features
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Submit PR with clear description

## Code Review

All submissions require review. Please be patient and responsive to feedback.

## Questions?

- Open an issue for bugs
- Start a discussion for questions
- Check existing issues first

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
