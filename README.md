# jar-runner
  **The library that can be used to run external jar packages in java projects.**  
  > Before running, it automatically downloads the dependencies of the jar package from the maven repository central, loads the dependencies using the isolated classloader, and then use reflection to invoke the methods in the external jar package.
  

**maven**:  
```xml
<dependency>
  <groupId>io.github.kerwin612</groupId>
  <artifactId>jar-runner</artifactId>
  <version>0.1</version>
</dependency>
```  
**gradle**:    
```groovy
implementation 'io.github.kerwin612:jar-runner:0.1'
```   

**example**:  
```java
JarRunner.load(Path loadedPath, boolean override, Path... jars).run(String className, String methodName, Object... args);
```
