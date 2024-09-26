//> using dep com.lihaoyi::fastparse:3.1.1

import scala.io.Source
import scala.util.Using

import java.io.File

// note: BigDecimal is well-supported and can represent a large range of numbers precisely
type Num = BigDecimal
// note: Errors are strings because this is effectively a PoC
type Error = String

/** Binary operation types
  */
enum Op:
  case Multiply, Divide, Add, Subtract, Exp

  override def toString: String =
    this match
      case Multiply => "*"
      case Divide   => "/"
      case Add      => "+"
      case Subtract => "-"
      case Exp      => "^"

/** Expression tree
  */
enum Expr:
  case Value(v: Num)
  case BinOp(l: Expr, op: Op, r: Expr)

  override def toString: String =
    this match
      case Value(v)        => s"$v"
      case BinOp(l, op, r) => s"($l$op$r)"

object Expr:
  // convenience syntax, mainly for tests
  def apply(v: Num): Expr                  = Expr.Value(v)
  def apply(l: Num, o: Op, r: Num): Expr   = Expr.BinOp(apply(l), o, apply(r))
  def apply(l: Num, o: Op, r: Expr): Expr  = Expr.BinOp(apply(l), o, r)
  def apply(l: Expr, o: Op, r: Num): Expr  = Expr.BinOp(l, o, apply(r))
  def apply(l: Expr, o: Op, r: Expr): Expr = Expr.BinOp(l, o, r)

/** Parser for calculator expressions, producing the expression tree
  */
object CalculatorParser:
  import fastparse.*
  import fastparse.SingleLineWhitespace.*

  // note: because we're whitespace insensitive, need to use double ~~ and repX to avoid consuming whitespace while
  //       parsing decimals
  private def int[$: P]: P[Expr] =
    P(CharIn("0-9").repX(min = 1).!.map(s => Expr.Value(s.toInt)))
  private def decimal[$: P]: P[Expr] =
    P((CharIn("0-9").repX(min = 1) ~~ "." ~~/ CharIn("0-9").repX(min = 1)).!).map(s => Expr.Value(s.toDouble))
  // note: I split the definition here because originally I wanted to separate integer arithmetic from decimals
  private def number[$: P]: P[Expr] = P(decimal | int)

  private def opDivide[$: P]: P[Op]   = P("/").map(_ => Op.Divide)
  private def opMultiply[$: P]: P[Op] = P("*").map(_ => Op.Multiply)
  private def opAdd[$: P]: P[Op]      = P("+").map(_ => Op.Add)
  private def opSubtract[$: P]: P[Op] = P("-").map(_ => Op.Subtract)
  private def opExp[$: P]: P[Op]      = P("^").map(_ => Op.Exp)

  private def parens[$: P]: P[Expr]       = P("(" ~/ binAddSubtract ~/ ")")
  private def parensLowest[$: P]: P[Expr] = P(number | parens)

  private def binExp[$: P]: P[Expr] =
    P(parensLowest ~ (opExp ~/ parensLowest).rep).map(binOpSequence)
  private def binDivideMultiply[$: P]: P[Expr] =
    P(binExp ~ ((opDivide | opMultiply) ~/ binExp).rep).map(binOpSequence)
  private def binAddSubtract[$: P]: P[Expr] =
    P(binDivideMultiply ~ ((opAdd | opSubtract) ~/ binDivideMultiply).rep).map(binOpSequence)

  private def expr[$: P]: P[Expr] = P(binAddSubtract ~ End)

  private def binOpSequence(l: Expr, rSeq: Seq[(Op, Expr)]): Expr =
    rSeq.foldLeft(l):
      case (l, (o, r)) => Expr.BinOp(l, o, r)

  def apply(value: String): Either[String, Expr] =
    parse[Expr](value, ctx => expr(using ctx)) match
      case Parsed.Success(value, _) => Right(value)
      case f: Parsed.Failure        => Left(f.toString())

/** Evaluator for calculator expressions, producing a numeric result
  */
object Calculator:
  def apply(expr: Expr): Num =
    expr match
      case Expr.Value(v)        => v
      case Expr.BinOp(l, op, r) => evalOp(apply(l), op, apply(r))

  private def evalOp(l: Num, op: Op, r: Num): Num =
    op match
      case Op.Multiply => l * r
      case Op.Divide   => l / r
      case Op.Add      => l + r
      case Op.Subtract => l - r
      case Op.Exp      => l.pow(r.toIntExact)

private def evalExprLine(line: String): Unit =
  CalculatorParser(line).map(Calculator.apply) match
    case Right(result) => System.out.println(s"$line = $result")
    case Left(error)   => System.out.println(s"$line !! $error")

private def evalExprsFromFile(file: File): Unit =
  Using(Source.fromFile(file))(_.getLines().foreach(evalExprLine))

// note: no good error message is number of arguments mismatches
@main def runCalculator(filename: String): Unit =
  val f = File(filename)
  if (f.isFile) evalExprsFromFile(f)
  else System.err.println("Error: filename does not exist/is not a file")
