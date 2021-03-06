package astnodes.builders.function;

import java.util.EmptyStackException;

import org.antlr.v4.runtime.ParserRuleContext;

import parsing.InitDeclContextWrapper;
import antlr.FunctionParser.Additive_expressionContext;
import antlr.FunctionParser.And_expressionContext;
import antlr.FunctionParser.ArrayIndexingContext;
import antlr.FunctionParser.Assign_exprContext;
import antlr.FunctionParser.Bit_and_expressionContext;
import antlr.FunctionParser.Block_starterContext;
import antlr.FunctionParser.BreakStatementContext;
import antlr.FunctionParser.Cast_expressionContext;
import antlr.FunctionParser.Cast_targetContext;
import antlr.FunctionParser.Closing_curlyContext;
import antlr.FunctionParser.ConditionContext;
import antlr.FunctionParser.Conditional_expressionContext;
import antlr.FunctionParser.ContinueStatementContext;
import antlr.FunctionParser.DeclByClassContext;
import antlr.FunctionParser.Do_statementContext;
import antlr.FunctionParser.Else_statementContext;
import antlr.FunctionParser.Equality_expressionContext;
import antlr.FunctionParser.Exclusive_or_expressionContext;
import antlr.FunctionParser.ExprContext;
import antlr.FunctionParser.Expr_statementContext;
import antlr.FunctionParser.For_init_statementContext;
import antlr.FunctionParser.For_statementContext;
import antlr.FunctionParser.FuncCallContext;
import antlr.FunctionParser.Function_argumentContext;
import antlr.FunctionParser.Function_argument_listContext;
import antlr.FunctionParser.GotoStatementContext;
import antlr.FunctionParser.IdentifierContext;
import antlr.FunctionParser.If_statementContext;
import antlr.FunctionParser.IncDecOpContext;
import antlr.FunctionParser.Inc_decContext;
import antlr.FunctionParser.Inclusive_or_expressionContext;
import antlr.FunctionParser.InitDeclSimpleContext;
import antlr.FunctionParser.InitDeclWithAssignContext;
import antlr.FunctionParser.InitDeclWithCallContext;
import antlr.FunctionParser.Initializer_listContext;
import antlr.FunctionParser.LabelContext;
import antlr.FunctionParser.MemberAccessContext;
import antlr.FunctionParser.Multiplicative_expressionContext;
import antlr.FunctionParser.Opening_curlyContext;
import antlr.FunctionParser.Or_expressionContext;
import antlr.FunctionParser.Primary_expressionContext;
import antlr.FunctionParser.PtrMemberAccessContext;
import antlr.FunctionParser.Relational_expressionContext;
import antlr.FunctionParser.ReturnStatementContext;
import antlr.FunctionParser.Shift_expressionContext;
import antlr.FunctionParser.StatementContext;
import antlr.FunctionParser.StatementsContext;
import antlr.FunctionParser.Switch_statementContext;
import antlr.FunctionParser.Type_nameContext;
import antlr.FunctionParser.Unary_expressionContext;
import antlr.FunctionParser.While_statementContext;
import astnodes.ASTNode;
import astnodes.ASTNodeBuilder;
import astnodes.builders.ClassDefBuilder;
import astnodes.builders.IdentifierDeclBuilder;
import astnodes.declarations.ClassDefStatement;
import astnodes.declarations.IdentifierDecl;
import astnodes.expressions.AdditiveExpression;
import astnodes.expressions.AndExpression;
import astnodes.expressions.Argument;
import astnodes.expressions.ArgumentList;
import astnodes.expressions.ArrayIndexing;
import astnodes.expressions.AssignmentExpr;
import astnodes.expressions.BitAndExpression;
import astnodes.expressions.CallExpression;
import astnodes.expressions.Callee;
import astnodes.expressions.CastExpression;
import astnodes.expressions.CastTarget;
import astnodes.expressions.ConditionalExpression;
import astnodes.expressions.EqualityExpression;
import astnodes.expressions.ExclusiveOrExpression;
import astnodes.expressions.Expression;
import astnodes.expressions.Identifier;
import astnodes.expressions.IncDec;
import astnodes.expressions.IncDecOp;
import astnodes.expressions.InclusiveOrExpression;
import astnodes.expressions.InitializerList;
import astnodes.expressions.MemberAccess;
import astnodes.expressions.MultiplicativeExpression;
import astnodes.expressions.OrExpression;
import astnodes.expressions.PrimaryExpression;
import astnodes.expressions.PtrMemberAccess;
import astnodes.expressions.RelationalExpression;
import astnodes.expressions.ShiftExpression;
import astnodes.expressions.UnaryExpression;
import astnodes.statements.BlockCloser;
import astnodes.statements.BlockStarter;
import astnodes.statements.BreakStatement;
import astnodes.statements.CompoundStatement;
import astnodes.statements.Condition;
import astnodes.statements.ContinueStatement;
import astnodes.statements.DoStatement;
import astnodes.statements.ElseStatement;
import astnodes.statements.ExpressionStatement;
import astnodes.statements.ForInit;
import astnodes.statements.ForStatement;
import astnodes.statements.GotoStatement;
import astnodes.statements.IdentifierDeclStatement;
import astnodes.statements.IfStatement;
import astnodes.statements.Label;
import astnodes.statements.ReturnStatement;
import astnodes.statements.Statement;
import astnodes.statements.SwitchStatement;
import astnodes.statements.WhileStatement;

/**
 * The FunctionContentBuilder is invoked while walking the
 * parse tree to create ASTs for the contents of functions,
 * i.e., the first-level compound statements of functions.
 * 
 * Since the fuzzy parser avoids using nested grammar rules
 * as these rules often require reading all tokens of a file
 * only to realize that the default rule must be taken, the
 * most difficult task this code fulfills is to produce a
 * correctly nested AST.
 */

public class FunctionContentBuilder extends ASTNodeBuilder
{
	ContentBuilderStack stack = new ContentBuilderStack();
	NestingReconstructor nesting = new NestingReconstructor(stack);
	
	// exitStatements is called when the entire
	// function-content has been walked

	public void exitStatements(StatementsContext ctx)
	{
		if(stack.size() != 1)
			throw new RuntimeException("Broken stack while parsing");
		
	}
	
	// For all statements, begin by pushing a Statement Object
	// onto the stack.

	public void enterStatement(StatementContext ctx)
	{
		ASTNode statementItem = new Statement();
		statementItem.initializeFromContext(ctx);
		stack.push(statementItem);
	}

	// Mapping of grammar-rules to CodeItems.

	public void enterOpeningCurly(Opening_curlyContext ctx)
	{
		replaceTopOfStack(new CompoundStatement());
	}

	public void enterClosingCurly(Closing_curlyContext ctx)
	{
		replaceTopOfStack(new BlockCloser());
	}

	public void enterBlockStarter(Block_starterContext ctx)
	{
		replaceTopOfStack(new BlockStarter());
	}

	public void enterExprStatement(Expr_statementContext ctx)
	{
		replaceTopOfStack(new ExpressionStatement());
	}

	public void enterIf(If_statementContext ctx)
	{
		replaceTopOfStack(new IfStatement());
	}

	public void enterFor(For_statementContext ctx)
	{
		replaceTopOfStack(new ForStatement());
	}

	public void enterWhile(While_statementContext ctx)
	{
		replaceTopOfStack(new WhileStatement());
	}

	public void enterDo(Do_statementContext ctx)
	{
		replaceTopOfStack(new DoStatement());
	}

	public void enterElse(Else_statementContext ctx)
	{
		replaceTopOfStack(new ElseStatement());
	}

	public void exitStatement(StatementContext ctx)
	{
		if(stack.size() == 0)
			throw new RuntimeException();

		ASTNode itemToRemove = stack.peek();
		itemToRemove.initializeFromContext(ctx);

		if(itemToRemove instanceof BlockCloser){
			closeCompoundStatement();
			return;
		}

		// We keep Block-starters and compound items
		// on the stack. They are removed by following
		// statements.
		if(itemToRemove instanceof BlockStarter ||
				itemToRemove instanceof CompoundStatement)
			return;

		nesting.consolidate();	
	}

	private void closeCompoundStatement()
	{
		stack.pop(); // remove 'CloseBlock'
		
		CompoundStatement compoundItem = (CompoundStatement) stack.pop();
		nesting.consolidateBlockStarters(compoundItem);		
	}

	// Expression handling

	public void enterExpression(ExprContext ctx)
	{
		Expression expression = new Expression();
		stack.push(expression);
	}

	public void exitExpression(ExprContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterAssignment(Assign_exprContext ctx)
	{	
		AssignmentExpr expr = new AssignmentExpr();
		stack.push(expr);
	}

	public void exitAssignment(Assign_exprContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterConditionalExpr(Conditional_expressionContext ctx)
	{
		ConditionalExpression expr = new ConditionalExpression();
		stack.push(expr);
	}

	public void exitConditionalExpr(Conditional_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterOrExpression(Or_expressionContext ctx)
	{
		OrExpression expr = new OrExpression();
		stack.push(expr);
	}

	public void exitrOrExpression(Or_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterAndExpression(And_expressionContext ctx)
	{
		AndExpression expr = new AndExpression();
		stack.push(expr);
	}

	public void exitAndExpression(And_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterInclusiveOrExpression(Inclusive_or_expressionContext ctx)
	{
		InclusiveOrExpression expr = new InclusiveOrExpression();
		stack.push(expr);
	}

	public void exitInclusiveOrExpression(Inclusive_or_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterExclusiveOrExpression(Exclusive_or_expressionContext ctx)
	{
		ExclusiveOrExpression expr = new ExclusiveOrExpression();
		stack.push(expr);
	}

	public void exitExclusiveOrExpression(Exclusive_or_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterBitAndExpression(Bit_and_expressionContext ctx)
	{
		BitAndExpression expr = new BitAndExpression();
		stack.push(expr);
	}

	public void enterEqualityExpression(Equality_expressionContext ctx)
	{
		EqualityExpression expr = new EqualityExpression();
		stack.push(expr);
	}

	public void exitEqualityExpression(Equality_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void exitBitAndExpression(Bit_and_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}	

	public void enterRelationalExpression(Relational_expressionContext ctx)
	{
		RelationalExpression expr = new RelationalExpression();
		stack.push(expr);
	}

	public void exitRelationalExpression(Relational_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterShiftExpression(Shift_expressionContext ctx)
	{
		ShiftExpression expr = new ShiftExpression();
		stack.push(expr);
	}

	public void exitShiftExpression(Shift_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterAdditiveExpression(Additive_expressionContext ctx)
	{
		AdditiveExpression expr = new AdditiveExpression();
		stack.push(expr);
	}

	public void exitAdditiveExpression(Additive_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}	

	public void enterMultiplicativeExpression(Multiplicative_expressionContext ctx)
	{
		MultiplicativeExpression expr = new MultiplicativeExpression();
		stack.push(expr);
	}

	public void exitMultiplicativeExpression(Multiplicative_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterCastExpression(Cast_expressionContext ctx)
	{
		CastExpression expr = new CastExpression();
		stack.push(expr);
	}

	public void exitCastExpression(Cast_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterCast_target(Cast_targetContext ctx)
	{
		CastTarget expr = new CastTarget();
		stack.push(expr);
	}

	public void exitCast_target(Cast_targetContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterFuncCall(FuncCallContext ctx)
	{
		CallExpression expr = new CallExpression();
		stack.push(expr);
	}

	public void exitFuncCall(FuncCallContext ctx)
	{
		introduceCalleeNode();
		nesting.consolidateSubExpression(ctx);
	}

	private void introduceCalleeNode()
	{
		CallExpression expr;
		try{
			expr = (CallExpression) stack.peek();
		}catch(EmptyStackException ex){
			return;
		}

		ASTNode child = expr.getChild(0);
		if(child == null) return;

		Callee callee = new Callee(); 
		callee.addChild(child);
		expr.replaceFirstChild(callee);
	}

	public void enterArgumentList(Function_argument_listContext ctx)
	{
		ArgumentList expr = new ArgumentList();
		stack.push(expr);
	}

	public void exitArgumentList(Function_argument_listContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterCondition(ConditionContext ctx)
	{
		Condition expr = new Condition();
		stack.push(expr);
	}

	public void exitCondition(ConditionContext ctx)
	{	
		Condition cond = (Condition) stack.pop();
		cond.initializeFromContext(ctx);
		nesting.addItemToParent(cond);
	}

	public void enterDeclByClass(DeclByClassContext ctx)
	{
		ClassDefBuilder classDefBuilder = new ClassDefBuilder();
		classDefBuilder.createNew(ctx);
		classDefBuilder.setName(ctx.class_def().class_name());
		replaceTopOfStack(classDefBuilder.getItem());
	}

	public void exitDeclByClass()
	{
		nesting.consolidate();
	}

	public void enterInitDeclSimple(InitDeclSimpleContext ctx)
	{				
		ASTNode identifierDecl = buildDeclarator(ctx);
		stack.push(identifierDecl);	
	}

	public void exitInitDeclSimple()
	{
		IdentifierDecl identifierDecl = (IdentifierDecl) stack.pop();
		ASTNode stmt =  stack.peek();
		stmt.addChild(identifierDecl);
	}

	public void enterInitDeclWithAssign(InitDeclWithAssignContext ctx)
	{
		IdentifierDecl identifierDecl = buildDeclarator(ctx);				
		stack.push(identifierDecl);	
	}

	public void exitInitDeclWithAssign(InitDeclWithAssignContext ctx)
	{
		IdentifierDecl identifierDecl = (IdentifierDecl) stack.pop();

		Expression lastChild = (Expression) identifierDecl.popLastChild();
		AssignmentExpr assign = new AssignmentExpr();
		assign.initializeFromContext(ctx);

		// watchout here, we're not making a copy.
		// This is also a bit of a hack. As we go up,
		// we introduce an artificial assignment-node.

		assign.addChild(identifierDecl.getName());
		assign.addChild(lastChild);

		identifierDecl.addChild(assign);

		ASTNode stmt =  stack.peek();
		stmt.addChild(identifierDecl);
	}

	public void enterInitDeclWithCall(InitDeclWithCallContext ctx)
	{
		ASTNode identifierDecl = buildDeclarator(ctx);
		stack.push(identifierDecl);	
	}

	public void exitInitDeclWithCall()
	{
		IdentifierDecl identifierDecl = (IdentifierDecl) stack.pop();
		ASTNode stmt =  stack.peek();
		stmt.addChild(identifierDecl);
	}

	private IdentifierDecl buildDeclarator(ParserRuleContext ctx)
	{
		InitDeclContextWrapper wrappedContext = new InitDeclContextWrapper(ctx);
		ParserRuleContext typeName = getTypeFromParent();
		IdentifierDeclBuilder declBuilder = new IdentifierDeclBuilder();
		declBuilder.createNew(ctx);
		declBuilder.setType(wrappedContext, typeName);
		IdentifierDecl identifierDecl = (IdentifierDecl) declBuilder.getItem();
		return identifierDecl;
	}

	private ParserRuleContext getTypeFromParent()
	{
		ASTNode parentItem =  stack.peek();
		ParserRuleContext typeName;
		if(parentItem instanceof IdentifierDeclStatement)
			typeName = ((IdentifierDeclStatement) parentItem).getTypeNameContext();
		else if (parentItem instanceof ClassDefStatement)
			typeName = ((ClassDefStatement) parentItem).getName().getParseTreeNodeContext();
		else
			throw new RuntimeException("No matching declaration statement/class definiton for init declarator");
		return typeName;
	}

	public void enterIncDec(Inc_decContext ctx)
	{
		IncDec expr = new IncDec();
		stack.push(expr);
	}

	public void exitIncDec(Inc_decContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterArrayIndexing(ArrayIndexingContext ctx)
	{
		ArrayIndexing expr = new ArrayIndexing();
		stack.push(expr);
	}

	public void exitArrayIndexing(ArrayIndexingContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterMemberAccess(MemberAccessContext ctx)
	{
		MemberAccess expr = new MemberAccess();
		stack.push(expr);
	}

	public void exitMemberAccess(MemberAccessContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterIncDecOp(IncDecOpContext ctx)
	{
		IncDecOp expr = new IncDecOp();
		stack.push(expr);
	}

	public void exitIncDecOp(IncDecOpContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterPrimary(Primary_expressionContext ctx)
	{
		PrimaryExpression expr = new PrimaryExpression();
		stack.push(expr);
	}
	
	public void exitPrimary(Primary_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterUnaryExpression(Unary_expressionContext ctx)
	{
		UnaryExpression expr = new UnaryExpression();
		stack.push(expr);
	}

	public void exitUnaryExpression(Unary_expressionContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterIdentifier(IdentifierContext ctx)
	{
		Identifier expr = new Identifier();
		stack.push(expr);
	}

	public void exitIdentifier(IdentifierContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterArgument(Function_argumentContext ctx)
	{
		Argument expr = new Argument();
		stack.push(expr);
	}

	public void exitArgument(Function_argumentContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterInitializerList(Initializer_listContext ctx)
	{
		InitializerList expr = new InitializerList();
		stack.push(expr);
	}

	public void exitInitializerList(Initializer_listContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterPtrMemberAccess(PtrMemberAccessContext ctx)
	{
		PtrMemberAccess expr = new PtrMemberAccess();
		stack.push(expr);
	}

	public void exitPtrMemberAccess(PtrMemberAccessContext ctx)
	{
		nesting.consolidateSubExpression(ctx);
	}

	public void enterInitFor(For_init_statementContext ctx)
	{
		ForInit expr = new ForInit();
		stack.push(expr);
	}

	public void exitInitFor(For_init_statementContext ctx)
	{
		ASTNode node = stack.pop();
		node.initializeFromContext(ctx);
		ForStatement forStatement = (ForStatement) stack.peek();
		forStatement.addChild(node);
	}

	public void enterSwitchStatement(Switch_statementContext ctx)
	{
		replaceTopOfStack(new SwitchStatement());
	}

	public void enterLabel(LabelContext ctx)
	{
		replaceTopOfStack(new Label());
	}

	public void enterReturnStatement(ReturnStatementContext ctx)
	{
		replaceTopOfStack(new ReturnStatement());
	}

	public void enterBreakStatement(BreakStatementContext ctx)
	{
		replaceTopOfStack(new BreakStatement());
	}

	public void enterContinueStatement(ContinueStatementContext ctx)
	{
		replaceTopOfStack(new ContinueStatement());
	}

	public void enterGotoStatement(GotoStatementContext ctx)
	{
		replaceTopOfStack(new GotoStatement());
	}

	@Override
	public void createNew(ParserRuleContext ctx)
	{
		item = new CompoundStatement();
		CompoundStatement rootItem = (CompoundStatement) item;
		item.initializeFromContext(ctx);
		stack.push(rootItem);
	}

	public void addLocalDecl(IdentifierDecl decl)
	{
		IdentifierDeclStatement declStmt = (IdentifierDeclStatement) stack.peek();
		declStmt.addChild(decl);
	}

	public void enterDeclByType(ParserRuleContext ctx, Type_nameContext type_nameContext)
	{
		IdentifierDeclStatement declStmt = new IdentifierDeclStatement();
		declStmt.initializeFromContext(ctx);
		declStmt.setTypeNameContext(type_nameContext);

		if(stack.peek() instanceof Statement)
			replaceTopOfStack(declStmt);
		else
			stack.push(declStmt);
	}

	public void exitDeclByType()
	{
		nesting.consolidate();
	}

	protected void replaceTopOfStack(ASTNode item)
	{
		stack.pop();
		stack.push(item);
	}
	
}
