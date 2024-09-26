## Take-home exercise solution

This repository contains a small arithmetic expression calculator, that supports:

* decimal literals (with precision of [BigDecimal])
* precedence rules for `^ * / + -`
* parentheses
* evaluation of expressions from a file

## Running

Install [scala-cli], either by using a version manager like [mise]/[asdf] or by
[getting Scala 3.5](https://scala-lang.org/download/) from the website (which since version 3.5 bundles scala-cli).

To run the tests, issue `scala-cli test .`.

To run against a file of expressions:

`scala-cli run . -- <filename>`

## Developing

It is possible to import the project into IntelliJ/VsCode by first running `scala-ci setup-ide .` and then importing as
a BSP project.

The following libraries were used:

* [fastparse] - parser combinator library
* [munit] - unit tests

## Notes

I chose `scala-cli` for the project because it recently became bundled and I wanted to try it out - it is much more
lightweight than setting up sbt. Fastparse is a library I had heard much about and now got an opportunity to use. The
approach to precedence is the [Precedence climbing method][precedence-climbing-method]. I split out the evaluation from
the parsing mostly to be able to test at this point, though future changes that involve rewriting expressions or more
complex evaluation strategies would also benefit from the separation.

[asdf]: https://asdf-vm.com/
[BigDecimal]: https://www.scala-lang.org/api/current/scala/math/BigDecimal.html
[fastparse]: https://com-lihaoyi.github.io/fastparse/#GettingStarted
[mise]: https://mise.jdx.dev/
[precedence-climbing-method]: https://en.wikipedia.org/wiki/Operator-precedence_parser#Precedence_climbing_method
[scala-cli]: https://scala-cli.virtuslab.org/