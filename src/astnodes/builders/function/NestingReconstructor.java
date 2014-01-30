package astnodes.builders.function;

import org.antlr.v4.runtime.ParserRuleContext;

import astnodes.ASTNode;
import astnodes.expressions.Expression;
import astnodes.statements.BlockStarter;
import astnodes.statements.CompoundStatement;
import astnodes.statements.DoStatement;
import astnodes.statements.ElseStatement;
import astnodes.statements.ExpressionHolder;
import astnodes.statements.IfStatement;
import astnodes.statements.WhileStatement;

public class NestingReconstructor {

	ContentBuilderStack stack;
	
	public NestingReconstructor(ContentBuilderStack aStack)
	{
		stack = aStack;
	}
	
	protected void addItemToParent(ASTNode expression)
	{
		ASTNode topOfStack = stack.peek();
		topOfStack.addChild(expression);
	}
	
	protected void consolidateSubExpression(ParserRuleContext ctx)
	{
		Expression expression = (Expression) stack.pop();
		expression.initializeFromContext(ctx);
		if(!(expression instanceof ExpressionHolder))
			expression = pullUpOnlyChild(expression);
		addItemToParent(expression);
	}
	
	private Expression pullUpOnlyChild(Expression expression)
	{
		if(expression.getChildCount() == 1)
			expression = (Expression) expression.getChild(0);
		return expression;
	}
	
	protected void consolidate()
	{

		ASTNode stmt = stack.pop();
		ASTNode topOfStack = null;

		if(stack.size() > 0)
			topOfStack = stack.peek();

		if(topOfStack instanceof CompoundStatement){
			CompoundStatement compound = (CompoundStatement)topOfStack;
			compound.addStatement(stmt);
		}else{
			consolidateBlockStarters(stmt);
		}

	}

	// Joins consecutive BlockStarters on the stack

	protected void consolidateBlockStarters(ASTNode stmt)
	{

		while(true){
			try{
				BlockStarter bItem = (BlockStarter) stack.peek();
				bItem = (BlockStarter) stack.pop();
				bItem.addChild(stmt);
				stmt = bItem;

				
				if(bItem instanceof IfStatement){

					if(stack.size() > 0 && stack.peek() instanceof ElseStatement){

						BlockStarter elseItem = (BlockStarter) stack.pop();
						elseItem.addChild(bItem);

						IfStatement lastIf = (IfStatement) stack.getIfInElseCase();
						if( lastIf != null){
							lastIf.setElseNode((ElseStatement) elseItem);
						}
							
						
						return;
					}
					
				}else if(bItem instanceof ElseStatement){
					// add else statement to the previous if-statement,
					// which has already been consolidated so we can return
					
					IfStatement lastIf = (IfStatement) stack.getIf();
					if(lastIf != null)
						lastIf.setElseNode((ElseStatement) bItem);
					else
						System.err.println("Warning: cannot find if for else");
					
					return;
				}else if(bItem instanceof WhileStatement){
					// add while statement to the previous do-statement
					// if that exists. Otherwise, do nothing special.
					
					DoStatement lastDo = stack.getDo();
					if(lastDo != null){
						lastDo.addChild( ((WhileStatement) bItem).getCondition() );
						return;
					}
					
				}

			}catch(ClassCastException ex){
				break;
			}
		}
		// Finally, add chain to top compound-item
		ASTNode root = stack.peek();
		root.addChild(stmt);
	}
}