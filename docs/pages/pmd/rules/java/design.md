---
title: Design
summary: Rules that help you discover design issues.
permalink: pmd_rules_java_design.html
folder: pmd/rules/java
sidebaractiveurl: /pmd_rules_java.html
editmepath: ../pmd-java/src/main/resources/category/java/design.xml
keywords: Design, AbstractClassWithoutAnyMethod, AvoidCatchingGenericException, AvoidDeeplyNestedIfStmts, AvoidRethrowingException, AvoidThrowingNewInstanceOfSameException, AvoidThrowingNullPointerException, AvoidThrowingRawExceptionTypes, ClassWithOnlyPrivateConstructorsShouldBeFinal, CollapsibleIfStatements, CouplingBetweenObjects, CyclomaticComplexity, DataClass, DoNotExtendJavaLangError, ExceptionAsFlowControl, ExcessiveClassLength, ExcessiveImports, ExcessiveMethodLength, ExcessiveParameterList, ExcessivePublicCount, FinalFieldCouldBeStatic, GodClass, ImmutableField, LawOfDemeter, LogicInversion, LoosePackageCoupling, ModifiedCyclomaticComplexity, NcssConstructorCount, NcssCount, NcssMethodCount, NcssTypeCount, NPathComplexity, SignatureDeclareThrowsException, SimplifiedTernary, SimplifyBooleanAssertion, SimplifyBooleanExpressions, SimplifyBooleanReturns, SimplifyConditional, SingularField, StdCyclomaticComplexity, SwitchDensity, TooManyFields, TooManyMethods, UselessOverridingMethod, UseObjectForClearerAPI, UseUtilityClass
language: Java
---
## AbstractClassWithoutAnyMethod

**Since:** PMD 4.2

**Priority:** High (1)

If an abstract class does not provides any methods, it may be acting as a simple data container
that is not meant to be instantiated. In this case, it is probably better to use a private or
protected constructor in order to prevent instantiation than make the class misleadingly abstract.

**This rule is defined by the following XPath expression:**
```
//ClassOrInterfaceDeclaration
    [@Abstract = 'true']
    [count(//MethodDeclaration) + count(//ConstructorDeclaration) = 0]
    [not(../Annotation/MarkerAnnotation/Name[typeIs('com.google.auto.value.AutoValue')])]
```

**Example(s):**

``` java
public abstract class Example {
    String field;
    int otherField;
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AbstractClassWithoutAnyMethod" />
```

## AvoidCatchingGenericException

**Since:** PMD 4.2.6

**Priority:** Medium (3)

Avoid catching generic exceptions such as NullPointerException, RuntimeException, Exception in try-catch block

**This rule is defined by the following XPath expression:**
```
//CatchStatement/FormalParameter/Type/ReferenceType/ClassOrInterfaceType[
    @Image='NullPointerException' or
    @Image='Exception' or
    @Image='RuntimeException']
```

**Example(s):**

``` java
package com.igate.primitive;

public class PrimitiveType {

    public void downCastPrimitiveType() {
        try {
            System.out.println(" i [" + i + "]");
        } catch(Exception e) {
            e.printStackTrace();
        } catch(RuntimeException e) {
            e.printStackTrace();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidCatchingGenericException" />
```

## AvoidDeeplyNestedIfStmts

**Since:** PMD 1.0

**Priority:** Medium (3)

Avoid creating deeply nested if-then statements since they are harder to read and error-prone to maintain.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.AvoidDeeplyNestedIfStmtsRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/AvoidDeeplyNestedIfStmtsRule.java)

**Example(s):**

``` java
public class Foo {
  public void bar(int x, int y, int z) {
    if (x>y) {
      if (y>z) {
        if (z==x) {
         // !! too deep
        }
      }
    }
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|problemDepth|3|The if statement depth reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidDeeplyNestedIfStmts" />
```

## AvoidRethrowingException

**Since:** PMD 3.8

**Priority:** Medium (3)

Catch blocks that merely rethrow a caught exception only add to code size and runtime complexity.

**This rule is defined by the following XPath expression:**
```
//CatchStatement[FormalParameter
 /VariableDeclaratorId/@Image = Block/BlockStatement/Statement
 /ThrowStatement/Expression/PrimaryExpression[count(PrimarySuffix)=0]/PrimaryPrefix/Name/@Image
 and count(Block/BlockStatement/Statement) =1]
```

**Example(s):**

``` java
public void bar() {
    try {
        // do something
    }  catch (SomeException se) {
       throw se;
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidRethrowingException" />
```

## AvoidThrowingNewInstanceOfSameException

**Since:** PMD 4.2.5

**Priority:** Medium (3)

Catch blocks that merely rethrow a caught exception wrapped inside a new instance of the same type only add to
code size and runtime complexity.

**This rule is defined by the following XPath expression:**
```
//CatchStatement[
  count(Block/BlockStatement/Statement) = 1
  and
  FormalParameter/Type/ReferenceType/ClassOrInterfaceType/@Image = Block/BlockStatement/Statement/ThrowStatement/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/ClassOrInterfaceType/@Image
  and
  count(Block/BlockStatement/Statement/ThrowStatement/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression) = 1
  and
  FormalParameter/VariableDeclaratorId = Block/BlockStatement/Statement/ThrowStatement/Expression/PrimaryExpression/PrimaryPrefix/AllocationExpression/Arguments/ArgumentList/Expression/PrimaryExpression/PrimaryPrefix/Name
  ]
```

**Example(s):**

``` java
public void bar() {
    try {
        // do something
    } catch (SomeException se) {
        // harmless comment
        throw new SomeException(se);
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidThrowingNewInstanceOfSameException" />
```

## AvoidThrowingNullPointerException

**Since:** PMD 1.8

**Priority:** High (1)

Avoid throwing NullPointerExceptions manually. These are confusing because most people will assume that the
virtual machine threw it.  To avoid a method being called with a null parameter, you may consider 
using an IllegalArgumentException instead, making it clearly seen as a programmer-initiated exception. 
However, there are better ways to handle this:

>*Effective Java, 3rd Edition, Item 72: Favor the use of standard exceptions*
>
>Arguably, every erroneous method invocation boils down to an illegal argument or state, 
but other exceptions are standardly used for certain kinds of illegal arguments and states. 
If a caller passes null in some parameter for which null values are prohibited, convention dictates that 
NullPointerException be thrown rather than IllegalArgumentException.

To implement that, you are encouraged to use `java.util.Objects.requireNonNull()`
(introduced in Java 1.7). This method is designed primarily for doing parameter
validation in methods and constructors with multiple parameters.

Your parameter validation could thus look like the following:
```
public class Foo {
    private String exampleValue;
      
    void setExampleValue(String exampleValue) {
      // check, throw and assignment in a single standard call
      this.exampleValue = Objects.requireNonNull(exampleValue, "exampleValue must not be null!");
    }
  }
```

**This rule is defined by the following XPath expression:**
```
//AllocationExpression/ClassOrInterfaceType[@Image='NullPointerException']
```

**Example(s):**

``` java
public class Foo {
    void bar() {
        throw new NullPointerException();
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidThrowingNullPointerException" />
```

## AvoidThrowingRawExceptionTypes

**Since:** PMD 1.8

**Priority:** High (1)

Avoid throwing certain exception types. Rather than throw a raw RuntimeException, Throwable,
Exception, or Error, use a subclassed exception or error instead.

**This rule is defined by the following XPath expression:**
```
//ThrowStatement//AllocationExpression
 /ClassOrInterfaceType[
 typeIsExactly('java.lang.Throwable')
or
 typeIsExactly('java.lang.Exception')
or
 typeIsExactly('java.lang.Error')
or
 typeIsExactly('java.lang.RuntimeException')
]
```

**Example(s):**

``` java
public class Foo {
    public void bar() throws Exception {
        throw new Exception();
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/AvoidThrowingRawExceptionTypes" />
```

## ClassWithOnlyPrivateConstructorsShouldBeFinal

**Since:** PMD 4.1

**Priority:** High (1)

A class with only private constructors should be final, unless the private constructor
is invoked by a inner class.

**This rule is defined by the following XPath expression:**
```
TypeDeclaration[count(../TypeDeclaration) = 1]/ClassOrInterfaceDeclaration
[@Final = 'false']
[count(./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/ConstructorDeclaration[@Private = 'true']) >= 1 ]
[count(./ClassOrInterfaceBody/ClassOrInterfaceBodyDeclaration/ConstructorDeclaration[(@Public = 'true') or (@Protected = 'true') or (@PackagePrivate = 'true')]) = 0 ]
[not(.//ClassOrInterfaceDeclaration)]
```

**Example(s):**

``` java
public class Foo {  //Should be final
    private Foo() { }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ClassWithOnlyPrivateConstructorsShouldBeFinal" />
```

## CollapsibleIfStatements

**Since:** PMD 3.1

**Priority:** Medium (3)

Sometimes two consecutive 'if' statements can be consolidated by separating their conditions with a boolean short-circuit operator.

**This rule is defined by the following XPath expression:**
```
//IfStatement[@Else='false']/Statement
 /IfStatement[@Else='false']
 |
//IfStatement[@Else='false']/Statement
 /Block[count(BlockStatement)=1]/BlockStatement
  /Statement/IfStatement[@Else='false']
```

**Example(s):**

``` java
void bar() {
    if (x) {            // original implementation
        if (y) {
            // do stuff
        }
    }
}

void bar() {
    if (x && y) {        // optimized implementation
        // do stuff
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/CollapsibleIfStatements" />
```

## CouplingBetweenObjects

**Since:** PMD 1.04

**Priority:** Medium (3)

This rule counts the number of unique attributes, local variables, and return types within an object. 
A number higher than the specified threshold can indicate a high degree of coupling.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.CouplingBetweenObjectsRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/CouplingBetweenObjectsRule.java)

**Example(s):**

``` java
import com.Blah;
import org.Bar;
import org.Bardo;

public class Foo {
    private Blah var1;
    private Bar var2;

    //followed by many imports of unique objects
    void ObjectC doWork() {
        Bardo var55;
        ObjectA var44;
        ObjectZ var93;
        return something;
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|threshold|20|Unique type reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/CouplingBetweenObjects" />
```

## CyclomaticComplexity

**Since:** PMD 1.03

**Priority:** Medium (3)

The complexity of methods directly affects maintenance costs and readability. Concentrating too much decisional logic
in a single method makes its behaviour hard to read and change.

Cyclomatic complexity assesses the complexity of a method by counting the number of decision points in a method,
plus one for the method entry. Decision points are places where the control flow jumps to another place in the
program. As such, they include all control flow statements, such as `if`, `while`, `for`, and `case`. For more
details on the calculation, see the documentation of the [Cyclo metric](/pmd_java_metrics_index.html#cyclomatic-complexity-cyclo).

Generally, numbers ranging from 1-4 denote low complexity, 5-7 denote moderate complexity, 8-10 denote
high complexity, and 11+ is very high complexity. By default, this rule reports methods with a complexity >= 10.
Additionnally, classes with many methods of moderate complexity get reported as well once the total of their
methods' complexities reaches 80, even if none of the methods was directly reported.

Reported methods should be broken down into several smaller methods. Reported classes should probably be broken down
into subcomponents.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.CyclomaticComplexityRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/CyclomaticComplexityRule.java)

**Example(s):**

``` java
class Foo {
  void baseCyclo() {                // Cyclo = 1
    highCyclo();
  }

  void highCyclo() {                // Cyclo = 10: reported!
    int x = 0, y = 2;
    boolean a = false, b = true;

    if (a && (y == 1 ? b : true)) { // +3
      if (y == x) {                 // +1
        while (true) {              // +1
          if (x++ < 20) {           // +1
            break;                  // +1
          }
        }
      } else if (y == t && !d) {    // +2
        x = a ? y : x;              // +1
      } else {
        x = 2;
      }
    }
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|cycloOptions||Choose options for the computation of Cyclo|yes. Delimiter is '\|'.|
|classReportLevel|80|Total class complexity reporting threshold|no|
|methodReportLevel|10|Cyclomatic complexity reporting threshold|no|
|reportLevel|10|<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f; font-size: 75%;">Deprecated</span>  Cyclomatic Complexity reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/CyclomaticComplexity" />
```

## DataClass

**Since:** PMD 6.0.0

**Priority:** Medium (3)

Data Classes are simple data holders, which reveal most of their state, and
without complex functionality. The lack of functionality may indicate that
their behaviour is defined elsewhere, which is a sign of poor data-behaviour
proximity. By directly exposing their internals, Data Classes break encapsulation,
and therefore reduce the system's maintainability and understandability. Moreover,
classes tend to strongly rely on their data representation, which makes for a brittle
design.

Refactoring a Data Class should focus on restoring a good data-behaviour proximity. In
most cases, that means moving the operations defined on the data back into the class.
In some other cases it may make sense to remove entirely the class and move the data
into the former client classes.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.DataClassRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/DataClassRule.java)

**Example(s):**

``` java
public class DataClass {

  public int bar = 0;
  public int na = 0;
  private int bee = 0;

  public void setBee(int n) {
    bee = n;
  }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/DataClass" />
```

## DoNotExtendJavaLangError

**Since:** PMD 4.0

**Priority:** Medium (3)

Errors are system exceptions. Do not extend them.

**This rule is defined by the following XPath expression:**
```
//ClassOrInterfaceDeclaration/ExtendsList/ClassOrInterfaceType
  [typeIs('java.lang.Error')]
```

**Example(s):**

``` java
public class Foo extends Error { }
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/DoNotExtendJavaLangError" />
```

## ExceptionAsFlowControl

**Since:** PMD 1.8

**Priority:** Medium (3)

Using Exceptions as form of flow control is not recommended as they obscure true exceptions when debugging.
Either add the necessary validation or use an alternate control structure.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExceptionAsFlowControlRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExceptionAsFlowControlRule.java)

**Example(s):**

``` java
public void bar() {
    try {
        try {
        } catch (Exception e) {
            throw new WrapperException(e);
            // this is essentially a GOTO to the WrapperException catch block
        }
    } catch (WrapperException e) {
        // do some more stuff
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExceptionAsFlowControl" />
```

## ExcessiveClassLength

**Since:** PMD 0.6

**Priority:** Medium (3)

Excessive class file lengths are usually indications that the class may be burdened with excessive 
responsibilities that could be provided by external classes or functions. In breaking these methods
apart the code becomes more manageable and ripe for reuse.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExcessiveClassLengthRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExcessiveClassLengthRule.java)

**Example(s):**

``` java
public class Foo {
    public void bar1() {
        // 1000 lines of code
    }
    public void bar2() {
        // 1000 lines of code
    }
    public void bar3() {
        // 1000 lines of code
    }

    public void barN() {
        // 1000 lines of code
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExcessiveClassLength" />
```

## ExcessiveImports

**Since:** PMD 1.04

**Priority:** Medium (3)

A high number of imports can indicate a high degree of coupling within an object. This rule 
counts the number of unique imports and reports a violation if the count is above the 
user-specified threshold.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExcessiveImportsRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExcessiveImportsRule.java)

**Example(s):**

``` java
import blah.blah.Baz;
import blah.blah.Bif;
// 18 others from the same package elided
public class Foo {
    public void doWork() {}
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExcessiveImports" />
```

## ExcessiveMethodLength

**Since:** PMD 0.6

**Priority:** Medium (3)

When methods are excessively long this usually indicates that the method is doing more than its
name/signature might suggest. They also become challenging for others to digest since excessive 
scrolling causes readers to lose focus.
Try to reduce the method length by creating helper methods and removing any copy/pasted code.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExcessiveMethodLengthRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExcessiveMethodLengthRule.java)

**Example(s):**

``` java
public void doSomething() {
    System.out.println("Hello world!");
    System.out.println("Hello world!");
    // 98 copies omitted for brevity.
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExcessiveMethodLength" />
```

## ExcessiveParameterList

**Since:** PMD 0.9

**Priority:** Medium (3)

Methods with numerous parameters are a challenge to maintain, especially if most of them share the
same datatype. These situations usually denote the need for new objects to wrap the numerous parameters.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExcessiveParameterListRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExcessiveParameterListRule.java)

**Example(s):**

``` java
public void addPerson(      // too many arguments liable to be mixed up
    int birthYear, int birthMonth, int birthDate, int height, int weight, int ssn) {

    . . . .
}
 
public void addPerson(      // preferred approach
    Date birthdate, BodyMeasurements measurements, int ssn) {

    . . . .
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExcessiveParameterList" />
```

## ExcessivePublicCount

**Since:** PMD 1.04

**Priority:** Medium (3)

Classes with large numbers of public methods and attributes require disproportionate testing efforts
since combinational side effects grow rapidly and increase risk. Refactoring these classes into
smaller ones not only increases testability and reliability but also allows new variations to be
developed easily.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ExcessivePublicCountRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ExcessivePublicCountRule.java)

**Example(s):**

``` java
public class Foo {
    public String value;
    public Bar something;
    public Variable var;
    // [... more more public attributes ...]

    public void doWork() {}
    public void doMoreWork() {}
    public void doWorkAgain() {}
    // [... more more public methods ...]
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ExcessivePublicCount" />
```

## FinalFieldCouldBeStatic

**Since:** PMD 1.1

**Priority:** Medium (3)

If a final field is assigned to a compile-time constant, it could be made static, thus saving overhead
in each object at runtime.

**This rule is defined by the following XPath expression:**
```
//FieldDeclaration
 [@Final='true' and @Static='false']
   /VariableDeclarator/VariableInitializer/Expression
    /PrimaryExpression[not(PrimarySuffix)]/PrimaryPrefix/Literal
```

**Example(s):**

``` java
public class Foo {
  public final int BAR = 42; // this could be static and save some space
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/FinalFieldCouldBeStatic" />
```

## GodClass

**Since:** PMD 5.0

**Priority:** Medium (3)

The God Class rule detects the God Class design flaw using metrics. God classes do too many things,
are very big and overly complex. They should be split apart to be more object-oriented.
The rule uses the detection strategy described in "Object-Oriented Metrics in Practice".
The violations are reported against the entire class.

See also the references:

Michele Lanza and Radu Marinescu. Object-Oriented Metrics in Practice:
Using Software Metrics to Characterize, Evaluate, and Improve the Design
of Object-Oriented Systems. Springer, Berlin, 1 edition, October 2006. Page 80.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.GodClassRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/GodClassRule.java)

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/GodClass" />
```

## ImmutableField

**Since:** PMD 2.0

**Priority:** Medium (3)

Identifies private fields whose values never change once they are initialized either in the declaration
of the field or by a constructor.  This helps in converting existing classes to becoming immutable ones.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ImmutableFieldRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ImmutableFieldRule.java)

**Example(s):**

``` java
public class Foo {
  private int x; // could be final
  public Foo() {
      x = 7;
  }
  public void foo() {
     int a = x + 2;
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|ignoredAnnotations|lombok.Setter \| lombok.Getter \| lombok.Builder \| lombok.Data \| lombok.RequiredArgsConstructor \| lombok.AllArgsConstructor \| lombok.Value \| lombok.NoArgsConstructor|Fully qualified names of the annotation types that should be ignored by this rule|yes. Delimiter is '\|'.|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ImmutableField" />
```

## LawOfDemeter

**Since:** PMD 5.0

**Priority:** Medium (3)

The Law of Demeter is a simple rule, that says "only talk to friends". It helps to reduce coupling between classes
or objects.

See also the references:

*   Andrew Hunt, David Thomas, and Ward Cunningham. The Pragmatic Programmer. From Journeyman to Master. Addison-Wesley Longman, Amsterdam, October 1999.;
*   K.J. Lieberherr and I.M. Holland. Assuring good style for object-oriented programs. Software, IEEE, 6(5):38–48, 1989.;
*   <http://www.ccs.neu.edu/home/lieber/LoD.html>
*   <http://en.wikipedia.org/wiki/Law_of_Demeter>

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.LawOfDemeterRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/LawOfDemeterRule.java)

**Example(s):**

``` java
public class Foo {
    /**
     * This example will result in two violations.
     */
    public void example(Bar b) {
        // this method call is ok, as b is a parameter of "example"
        C c = b.getC();

        // this method call is a violation, as we are using c, which we got from B.
        // We should ask b directly instead, e.g. "b.doItOnC();"
        c.doIt();

        // this is also a violation, just expressed differently as a method chain without temporary variables.
        b.getC().doIt();

        // a constructor call, not a method call.
        D d = new D();
        // this method call is ok, because we have create the new instance of D locally.
        d.doSomethingElse(); 
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/LawOfDemeter" />
```

## LogicInversion

**Since:** PMD 5.0

**Priority:** Medium (3)

Use opposite operator instead of negating the whole expression with a logic complement operator.

**This rule is defined by the following XPath expression:**
```
//UnaryExpressionNotPlusMinus[@Image='!']/PrimaryExpression/PrimaryPrefix/Expression[EqualityExpression or RelationalExpression]
```

**Example(s):**

``` java
public boolean bar(int a, int b) {

    if (!(a == b)) { // use !=
         return false;
     }

    if (!(a < b)) { // use >=
         return false;
    }

    return true;
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/LogicInversion" />
```

## LoosePackageCoupling

**Since:** PMD 5.0

**Priority:** Medium (3)

Avoid using classes from the configured package hierarchy outside of the package hierarchy, 
except when using one of the configured allowed classes.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.LoosePackageCouplingRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/LoosePackageCouplingRule.java)

**Example(s):**

``` java
package some.package;

import some.other.package.subpackage.subsubpackage.DontUseThisClass;

public class Bar {
    DontUseThisClass boo = new DontUseThisClass();
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|classes||Allowed classes|yes. Delimiter is ','.|
|packages||Restricted packages|yes. Delimiter is ','.|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/LoosePackageCoupling" />
```

## ModifiedCyclomaticComplexity

<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;">Deprecated</span> 

**Since:** PMD 5.1.2

**Priority:** Medium (3)

Complexity directly affects maintenance costs is determined by the number of decision points in a method 
plus one for the method entry.  The decision points include 'if', 'while', 'for', and 'case labels' calls.  
Generally, numbers ranging from 1-4 denote low complexity, 5-7 denote moderate complexity, 8-10 denote
high complexity, and 11+ is very high complexity. Modified complexity treats switch statements as a single
decision point.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.ModifiedCyclomaticComplexityRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/ModifiedCyclomaticComplexityRule.java)

**Example(s):**

``` java
public class Foo {    // This has a Cyclomatic Complexity = 9
1   public void example()  {
2       if (a == b)  {
3           if (a1 == b1) {
                fiddle();
4           } else if a2 == b2) {
                fiddle();
            }  else {
                fiddle();
            }
5       } else if (c == d) {
6           while (c == d) {
                fiddle();
            }
7        } else if (e == f) {
8           for (int n = 0; n < h; n++) {
                fiddle();
            }
        } else{
9           switch (z) {
                case 1:
                    fiddle();
                    break;
                case 2:
                    fiddle();
                    break;
                case 3:
                    fiddle();
                    break;
                default:
                    fiddle();
                    break;
            }
        }
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|showMethodsComplexity|true|Add method average violations to the report|no|
|showClassesComplexity|true|Add class average violations to the report|no|
|reportLevel|10|Cyclomatic Complexity reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/ModifiedCyclomaticComplexity" />
```

## NcssConstructorCount

<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;">Deprecated</span> 

**Since:** PMD 3.9

**Priority:** Medium (3)

This rule uses the NCSS (Non-Commenting Source Statements) algorithm to determine the number of lines
of code for a given constructor. NCSS ignores comments, and counts actual statements. Using this algorithm,
lines of code that are split are counted as one.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.NcssConstructorCountRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/NcssConstructorCountRule.java)

**Example(s):**

``` java
public class Foo extends Bar {
    public Foo() {
        super();





        //this constructor only has 1 NCSS lines
        super.foo();
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/NcssConstructorCount" />
```

## NcssCount

**Since:** PMD 6.0.0

**Priority:** Medium (3)

This rule uses the NCSS (Non-Commenting Source Statements) metric to determine the number of lines
of code in a class, method or constructor. NCSS ignores comments, blank lines, and only counts actual
statements. For more details on the calculation, see the documentation of
the [NCSS metric](/pmd_java_metrics_index.html#non-commenting-source-statements-ncss).

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.NcssCountRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/NcssCountRule.java)

**Example(s):**

``` java
import java.util.Collections;       // +0
import java.io.IOException;         // +0

class Foo {                         // +1, total Ncss = 12

  public void bigMethod()           // +1
      throws IOException {
    int x = 0, y = 2;               // +1
    boolean a = false, b = true;    // +1

    if (a || b) {                   // +1
      try {                         // +1
        do {                        // +1
          x += 2;                   // +1
        } while (x < 12);

        System.exit(0);             // +1
      } catch (IOException ioe) {   // +1
        throw new PatheticFailException(ioe); // +1
      }
    } else {
      assert false;                 // +1
    }
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|ncssOptions||Choose options for the calculation of Ncss|yes. Delimiter is '\|'.|
|methodReportLevel|12|Metric reporting threshold for methods|no|
|classReportLevel|250|Metric reporting threshold for classes|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/NcssCount" />
```

## NcssMethodCount

<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;">Deprecated</span> 

**Since:** PMD 3.9

**Priority:** Medium (3)

This rule uses the NCSS (Non-Commenting Source Statements) algorithm to determine the number of lines
of code for a given method. NCSS ignores comments, and counts actual statements. Using this algorithm,
lines of code that are split are counted as one.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.NcssMethodCountRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/NcssMethodCountRule.java)

**Example(s):**

``` java
public class Foo extends Bar {
    public int methd() {
        super.methd();






        //this method only has 1 NCSS lines
        return 1;
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/NcssMethodCount" />
```

## NcssTypeCount

<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;">Deprecated</span> 

**Since:** PMD 3.9

**Priority:** Medium (3)

This rule uses the NCSS (Non-Commenting Source Statements) algorithm to determine the number of lines
of code for a given type. NCSS ignores comments, and counts actual statements. Using this algorithm,
lines of code that are split are counted as one.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.NcssTypeCountRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/NcssTypeCountRule.java)

**Example(s):**

``` java
public class Foo extends Bar {
    public Foo() {
        //this class only has 6 NCSS lines
        super();





        super.foo();
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/NcssTypeCount" />
```

## NPathComplexity

**Since:** PMD 3.9

**Priority:** Medium (3)

The NPath complexity of a method is the number of acyclic execution paths through that method.
While cyclomatic complexity counts the number of decision points in a method, NPath counts the number of
full paths from the beginning to the end of the block of the method. That metric grows exponentially, as
it multiplies the complexity of statements in the same block. For more details on the calculation, see the
documentation of the [NPath metric](/pmd_java_metrics_index.html#npath-complexity-npath).

A threshold of 200 is generally considered the point where measures should be taken to reduce
complexity and increase readability.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.NPathComplexityRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/NPathComplexityRule.java)

**Example(s):**

``` java
public class Foo {
  public static void bar() { // Ncss = 252: reported!
    boolean a, b = true;
    try { // 2 * 2 + 2 = 6
      if (true) { // 2
        List buz = new ArrayList();
      }

      for(int i = 0; i < 19; i++) { // * 2
        List buz = new ArrayList();
      }
    } catch(Exception e) {
      if (true) { // 2
        e.printStackTrace();
      }
    }

    while (j++ < 20) { //  * 2
      List buz = new ArrayList();
    }

    switch(j) { // * 7
      case 1:
      case 2: break;
      case 3: j = 5; break;
      case 4: if (b && a) { bar(); } break;
      default: break;
    }

    do { // * 3
        List buz = new ArrayList();
    } while (a && j++ < 30);
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|minimum|200.0|<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f; font-size: 75%;">Deprecated</span>  Minimum reporting threshold|no|
|reportLevel|200|N-Path Complexity reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/NPathComplexity" />
```

## SignatureDeclareThrowsException

**Since:** PMD 1.2

**Priority:** Medium (3)

A method/constructor shouldn't explicitly throw the generic java.lang.Exception, since it
is unclear which exceptions that can be thrown from the methods. It might be
difficult to document and understand such vague interfaces. Use either a class
derived from RuntimeException or a checked exception.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.SignatureDeclareThrowsExceptionRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/SignatureDeclareThrowsExceptionRule.java)

**Example(s):**

``` java
public void foo() throws Exception {
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|IgnoreJUnitCompletely|false|Allow all methods in a JUnit testcase to throw Exceptions|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SignatureDeclareThrowsException" />
```

## SimplifiedTernary

**Since:** PMD 5.4.0

**Priority:** Medium (3)

Look for ternary operators with the form `condition ? literalBoolean : foo`
or `condition ? foo : literalBoolean`.

These expressions can be simplified respectively to
`condition || foo`  when the literalBoolean is true
`!condition && foo` when the literalBoolean is false
or
`!condition || foo` when the literalBoolean is true
`condition && foo`  when the literalBoolean is false

**This rule is defined by the following XPath expression:**
```
//ConditionalExpression[@Ternary='true'][not(PrimaryExpression/*/Literal) and (Expression/PrimaryExpression/*/Literal/BooleanLiteral)]
|
//ConditionalExpression[@Ternary='true'][not(Expression/PrimaryExpression/*/Literal) and (PrimaryExpression/*/Literal/BooleanLiteral)]
```

**Example(s):**

``` java
public class Foo {
    public boolean test() {
        return condition ? true : something(); // can be as simple as return condition || something();
    }

    public void test2() {
        final boolean value = condition ? false : something(); // can be as simple as value = !condition && something();
    }

    public boolean test3() {
        return condition ? something() : true; // can be as simple as return !condition || something();
    }

    public void test4() {
        final boolean otherValue = condition ? something() : false; // can be as simple as condition && something();
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SimplifiedTernary" />
```

## SimplifyBooleanAssertion

**Since:** PMD 3.6

**Priority:** Medium (3)

Avoid negation in an assertTrue or assertFalse test.

For example, rephrase:

    assertTrue(!expr);

as:

    assertFalse(expr);

**This rule is defined by the following XPath expression:**
```
//StatementExpression
[
.//Name[@Image='assertTrue' or  @Image='assertFalse']
and
PrimaryExpression/PrimarySuffix/Arguments/ArgumentList
 /Expression/UnaryExpressionNotPlusMinus[@Image='!']
/PrimaryExpression/PrimaryPrefix
]
[ancestor::ClassOrInterfaceDeclaration[//ClassOrInterfaceType[pmd-java:typeIs('junit.framework.TestCase')] or //MarkerAnnotation/Name[pmd-java:typeIs('org.junit.Test')]]]
```

**Example(s):**

``` java
public class SimpleTest extends TestCase {
    public void testX() {
        assertTrue("not empty", !r.isEmpty());  // replace with assertFalse("not empty", r.isEmpty())
        assertFalse(!r.isEmpty());              // replace with assertTrue(r.isEmpty())
    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SimplifyBooleanAssertion" />
```

## SimplifyBooleanExpressions

**Since:** PMD 1.05

**Priority:** Medium (3)

Avoid unnecessary comparisons in boolean expressions, they serve no purpose and impacts readability.

**This rule is defined by the following XPath expression:**
```
//EqualityExpression/PrimaryExpression
 /PrimaryPrefix/Literal/BooleanLiteral
```

**Example(s):**

``` java
public class Bar {
  // can be simplified to
  // bar = isFoo();
  private boolean bar = (isFoo() == true);

  public isFoo() { return false;}
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SimplifyBooleanExpressions" />
```

## SimplifyBooleanReturns

**Since:** PMD 0.9

**Priority:** Medium (3)

Avoid unnecessary if-then-else statements when returning a boolean. The result of
the conditional test can be returned instead.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.SimplifyBooleanReturnsRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/SimplifyBooleanReturnsRule.java)

**Example(s):**

``` java
public boolean isBarEqualTo(int x) {
    if (bar == x) {      // this bit of code...
        return true;
    } else {
        return false;
    }
}

public boolean isBarEqualTo(int x) {
    return bar == x;    // can be replaced with this
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SimplifyBooleanReturns" />
```

## SimplifyConditional

**Since:** PMD 3.1

**Priority:** Medium (3)

No need to check for null before an instanceof; the instanceof keyword returns false when given a null argument.

**This rule is defined by the following XPath expression:**
```
//Expression
 [ConditionalOrExpression
 [EqualityExpression[@Image='==']
  //NullLiteral
  and
  UnaryExpressionNotPlusMinus
   [@Image='!']//InstanceOfExpression[PrimaryExpression
     //Name/@Image = ancestor::ConditionalOrExpression/EqualityExpression
      /PrimaryExpression/PrimaryPrefix/Name/@Image]
  and
  (count(UnaryExpressionNotPlusMinus) + 1 = count(*))
 ]
or
ConditionalAndExpression
 [EqualityExpression[@Image='!=']//NullLiteral
 and
InstanceOfExpression
 [PrimaryExpression[count(PrimarySuffix[@ArrayDereference='true'])=0]
  //Name[not(contains(@Image,'.'))]/@Image = ancestor::ConditionalAndExpression
   /EqualityExpression/PrimaryExpression/PrimaryPrefix/Name/@Image]
 and
(count(InstanceOfExpression) + 1 = count(*))
 ]
]
```

**Example(s):**

``` java
class Foo {
  void bar(Object x) {
    if (x != null && x instanceof Bar) {
      // just drop the "x != null" check
    }
  }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SimplifyConditional" />
```

## SingularField

**Since:** PMD 3.1

**Priority:** Medium (3)

Fields whose scopes are limited to just single methods do not rely on the containing
object to provide them to other methods. They may be better implemented as local variables
within those methods.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.SingularFieldRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/SingularFieldRule.java)

**Example(s):**

``` java
public class Foo {
    private int x;  // no reason to exist at the Foo instance level
    public void foo(int y) {
     x = y + 5;
     return x;
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|disallowNotAssignment|false|Disallow violations where the first usage is not an assignment|no|
|checkInnerClasses|false|Check inner classes|no|
|ignoredAnnotations|lombok.Setter \| lombok.Getter \| lombok.Builder \| lombok.Data \| lombok.RequiredArgsConstructor \| lombok.AllArgsConstructor \| lombok.Value \| lombok.NoArgsConstructor|Fully qualified names of the annotation types that should be ignored by this rule|yes. Delimiter is '\|'.|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SingularField" />
```

## StdCyclomaticComplexity

<span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f;">Deprecated</span> 

**Since:** PMD 5.1.2

**Priority:** Medium (3)

Complexity directly affects maintenance costs is determined by the number of decision points in a method 
plus one for the method entry.  The decision points include 'if', 'while', 'for', and 'case labels' calls.  
Generally, numbers ranging from 1-4 denote low complexity, 5-7 denote moderate complexity, 8-10 denote
high complexity, and 11+ is very high complexity.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.StdCyclomaticComplexityRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/StdCyclomaticComplexityRule.java)

**Example(s):**

``` java
public class Foo {    // This has a Cyclomatic Complexity = 12
1   public void example()  {
2       if (a == b || (c == d && e == f))  { // Only one
3           if (a1 == b1) {
                fiddle();
4           } else if a2 == b2) {
                fiddle();
            }  else {
                fiddle();
            }
5       } else if (c == d) {
6           while (c == d) {
                fiddle();
            }
7        } else if (e == f) {
8           for (int n = 0; n < h; n++) {
                fiddle();
            }
        } else{
            switch (z) {
9               case 1:
                    fiddle();
                    break;
10              case 2:
                    fiddle();
                    break;
11              case 3:
                    fiddle();
                    break;
12              default:
                    fiddle();
                    break;
            }
        }
    }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|showMethodsComplexity|true|Add method average violations to the report|no|
|showClassesComplexity|true|Add class average violations to the report|no|
|reportLevel|10|Cyclomatic Complexity reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/StdCyclomaticComplexity" />
```

## SwitchDensity

**Since:** PMD 1.02

**Priority:** Medium (3)

A high ratio of statements to labels in a switch statement implies that the switch statement
is overloaded.  Consider moving the statements into new methods or creating subclasses based
on the switch variable.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.SwitchDensityRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/SwitchDensityRule.java)

**Example(s):**

``` java
public class Foo {
  public void bar(int x) {
    switch (x) {
      case 1: {
        // lots of statements
        break;
      } case 2: {
        // lots of statements
        break;
      }
    }
  }
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|topscore||Top score value|no|
|minimum||Minimum reporting threshold|no|
|sigma||Sigma value|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/SwitchDensity" />
```

## TooManyFields

**Since:** PMD 3.0

**Priority:** Medium (3)

Classes that have too many fields can become unwieldy and could be redesigned to have fewer fields,
possibly through grouping related fields in new objects.  For example, a class with individual 
city/state/zip fields could park them within a single Address field.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.TooManyFieldsRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/TooManyFieldsRule.java)

**Example(s):**

``` java
public class Person {   // too many separate fields
   int birthYear;
   int birthMonth;
   int birthDate;
   float height;
   float weight;
}

public class Person {   // this is more manageable
   Date birthDate;
   BodyMeasurements measurements;
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|maxfields|15|Max allowable fields|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/TooManyFields" />
```

## TooManyMethods

**Since:** PMD 4.2

**Priority:** Medium (3)

A class with too many methods is probably a good suspect for refactoring, in order to reduce its
complexity and find a way to have more fine grained objects.

**This rule is defined by the following XPath expression:**
```
//ClassOrInterfaceDeclaration/ClassOrInterfaceBody
     [
      count(./ClassOrInterfaceBodyDeclaration/MethodDeclaration/MethodDeclarator[
         not (
                starts-with(@Image,'get')
                or
                starts-with(@Image,'set')
                or
                starts-with(@Image,'is')
            )
      ]) > $maxmethods
   ]
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|maxmethods|10|The method count reporting threshold|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/TooManyMethods" />
```

## UselessOverridingMethod

**Since:** PMD 3.3

**Priority:** Medium (3)

The overriding method merely calls the same method defined in a superclass.

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.UselessOverridingMethodRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/UselessOverridingMethodRule.java)

**Example(s):**

``` java
public void foo(String bar) {
    super.foo(bar);      // why bother overriding?
}

public String foo() {
    return super.foo();  // why bother overriding?
}

@Id
public Long getId() {
    return super.getId();  // OK if 'ignoreAnnotations' is false, which is the default behavior
}
```

**This rule has the following properties:**

|Name|Default Value|Description|Multivalued|
|----|-------------|-----------|-----------|
|ignoreAnnotations|false|Ignore annotations|no|

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/UselessOverridingMethod" />
```

## UseObjectForClearerAPI

**Since:** PMD 4.2.6

**Priority:** Medium (3)

When you write a public method, you should be thinking in terms of an API. If your method is public, it means other class
will use it, therefore, you want (or need) to offer a comprehensive and evolutive API. If you pass a lot of information
as a simple series of Strings, you may think of using an Object to represent all those information. You'll get a simpler
API (such as doWork(Workload workload), rather than a tedious series of Strings) and more importantly, if you need at some
point to pass extra data, you'll be able to do so by simply modifying or extending Workload without any modification to
your API.

**This rule is defined by the following XPath expression:**
```
//MethodDeclaration[@Public]/MethodDeclarator/FormalParameters[
     count(FormalParameter/Type/ReferenceType/ClassOrInterfaceType[@Image = 'String']) > 3
]
```

**Example(s):**

``` java
public class MyClass {
    public void connect(String username,
        String pssd,
        String databaseName,
        String databaseAdress)
        // Instead of those parameters object
        // would ensure a cleaner API and permit
        // to add extra data transparently (no code change):
        // void connect(UserData data);
    {

    }
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/UseObjectForClearerAPI" />
```

## UseUtilityClass

**Since:** PMD 0.3

**Priority:** Medium (3)

For classes that only have static methods, consider making them utility classes.
Note that this doesn't apply to abstract classes, since their subclasses may
well include non-static methods.  Also, if you want this class to be a utility class,
remember to add a private constructor to prevent instantiation.
(Note, that this use was known before PMD 5.1.0 as UseSingleton).

**This rule is defined by the following Java class:** [net.sourceforge.pmd.lang.java.rule.design.UseUtilityClassRule](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/java/net/sourceforge/pmd/lang/java/rule/design/UseUtilityClassRule.java)

**Example(s):**

``` java
public class MaybeAUtility {
  public static void foo() {}
  public static void bar() {}
}
```

**Use this rule by referencing it:**
``` xml
<rule ref="category/java/design.xml/UseUtilityClass" />
```

