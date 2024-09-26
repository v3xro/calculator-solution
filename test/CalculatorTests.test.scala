//> using test.dep org.scalameta::munit::1.0.2

class CalculatorParserTests extends munit.FunSuite:
  test("constant value"):
    assertEquals(CalculatorParser("42"), Right(Expr.Value(42)))
    assertEquals(CalculatorParser("(42)"), Right(Expr.Value(42)))
    assertEquals(CalculatorParser("( (  42) )"), Right(Expr.Value(42)))
    assertEquals(CalculatorParser("3.14"), Right(Expr.Value(3.14)))
    assert(CalculatorParser("4 2").isLeft)
    assert(CalculatorParser("3 . 1 4").isLeft)
  test("addition/subtraction"):
    assertEquals(CalculatorParser("1.5 + 2"), Right(Expr(1.5, Op.Add, 2)))
    assertEquals(CalculatorParser("(4 + 2)"), Right(Expr(4, Op.Add, 2)))
    assertEquals(
      CalculatorParser("(4 + 2) - 5"),
      Right(Expr(Expr(4, Op.Add, 2), Op.Subtract, 5))
    )
  test("multiplication/division"):
    assertEquals(CalculatorParser("2 * 4.2"), Right(Expr(2, Op.Multiply, 4.2)))
    assertEquals(CalculatorParser("(4 * 2)"), Right(Expr(4, Op.Multiply, 2)))
    assertEquals(
      CalculatorParser("(4 * 2) / 1"),
      Right(Expr(Expr(4, Op.Multiply, 2), Op.Divide, 1))
    )
  test("exponentiation"):
    assertEquals(CalculatorParser("2 ^ 4"), Right(Expr(2, Op.Exp, 4)))
  test("precedence of binary operators"):
    assertEquals(
      CalculatorParser("1 + 2 * 3"),
      Right(Expr(1, Op.Add, Expr(2, Op.Multiply, 3)))
    )
    assertEquals(
      CalculatorParser("1 / 3 - 3"),
      Right(Expr(Expr(1, Op.Divide, 3), Op.Subtract, 3))
    )
    assertEquals(
      CalculatorParser("1 / (3 - 2)"),
      Right(Expr(1, Op.Divide, Expr(3, Op.Subtract, 2)))
    )
    assertEquals(
      CalculatorParser("1 - 2.3 * 3 ^ 4"),
      Right(Expr(1, Op.Subtract, Expr(2.3, Op.Multiply, Expr(3, Op.Exp, 4))))
    )

class CalculatorTests extends munit.FunSuite:
  private def calc(s: String) = CalculatorParser(s).map(Calculator.apply)

  test("calculator"):
    assertEquals(calc("42.2"), Right(BigDecimal(42.2)))
    assertEquals(calc("1 + 2 + 3"), Right(BigDecimal(6)))
    assertEquals(calc("1 + 2 * 3"), Right(BigDecimal(7)))
    assertEquals(calc("(4 + 2) - 5"), Right(BigDecimal(1)))
    assertEquals(calc("1 - 2.3 * 3 ^ 4"), Right(BigDecimal(-185.3)))
