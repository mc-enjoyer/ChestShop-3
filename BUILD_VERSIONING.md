# ChestShop Build Versioning

## Overview

The ChestShop plugin now includes version numbers in the built jar files to make it easier to identify different versions and track releases.

## Version Configuration

### pom.xml Configuration

The version is defined in the `pom.xml` file:

```xml
<groupId>com.acrobot.chestshop</groupId>
<artifactId>chestshop</artifactId>
<version>3.7.9</version>
```

### Jar Naming

The maven-jar-plugin is configured to include the version in the final jar name:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.6</version>
    <configuration>
        <finalName>ChestShop-${project.version}</finalName>
    </configuration>
</plugin>
```

## Build Output

When you run `mvn clean install`, the following files will be generated in the `target/` directory:

- `ChestShop-3.7.9.jar` - The main shaded jar with all dependencies
- `original-ChestShop-3.7.9.jar` - The original jar before shading

## Updating Versions

To update the version number:

1. Edit the `<version>` tag in `pom.xml`
2. Run `mvn clean install`
3. The new jar will be built with the updated version number

## Benefits

1. **Version Tracking**: Easy to identify which version of the plugin you're using
2. **Release Management**: Clear distinction between different releases
3. **Deployment**: No confusion about which version is deployed
4. **Backup**: Previous versions can be kept without naming conflicts

## Example

```bash
# Build the plugin
mvn clean install

# Output files:
# target/ChestShop-3.7.9.jar
# target/original-ChestShop-3.7.9.jar
```

## Notes

- The version number is automatically included in the jar filename
- The shade plugin works correctly with the versioned jar names
- All existing build processes remain the same
- No changes needed to deployment scripts 