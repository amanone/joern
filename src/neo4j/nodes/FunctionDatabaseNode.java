package neo4j.nodes;

import java.util.HashMap;
import java.util.Map;

import udg.CFGToUDGConverter;
import udg.useDefGraph.UseDefGraph;
import astnodes.ASTNode;
import astnodes.functionDef.FunctionDef;
import cfg.ASTToCFGConverter;
import cfg.CFG;
import ddg.CFGAndUDGToDefUseCFG;
import ddg.DDGCreator;
import ddg.DataDependenceGraph.DDG;
import ddg.DefUseCFG.DefUseCFG;

// Note: we currently use the FunctionDatabaseNode
// as a container for the Function. That's not very
// clean. We should have a sep. Function-Class.

public class FunctionDatabaseNode extends DatabaseNode
{
	FunctionDef astRoot;
	CFG cfg;
	UseDefGraph udg;
	DDG ddg;
	
	String signature;
	String name;
	
	ASTToCFGConverter astToCFG = new ASTToCFGConverter();
	CFGToUDGConverter cfgToUDG = new CFGToUDGConverter();
	CFGAndUDGToDefUseCFG udgAndCfgToDefUseCFG = new CFGAndUDGToDefUseCFG();
	DDGCreator ddgCreator = new DDGCreator();
	
	@Override
	public void initialize(Object node)
	{
		astRoot = (FunctionDef) node;
		cfg = astToCFG.convert(astRoot);
		udg = cfgToUDG.convert(cfg);
		DefUseCFG defUseCFG = udgAndCfgToDefUseCFG.convert(cfg, udg);
		ddg = ddgCreator.createForDefUseCFG(defUseCFG);
		
		setSignature(astRoot);
	}

	@Override public Map<String, Object> createProperties()
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(NodeKeys.TYPE, "Function");
		properties.put(NodeKeys.LOCATION, this.getLocation());
		properties.put(NodeKeys.NAME, this.getName());
		// properties.put("signature", this.getSignature());
		return properties;
	}
	
	public String getName()
	{
		return astRoot.name.getEscapedCodeStr();
	}

	public ASTNode getASTRoot()
	{
		return astRoot;
	}

	public CFG getCFG()
	{
		return cfg;
	}
	
	public UseDefGraph getUDG()
	{
		return udg;
	}

	public DDG getDDG()
	{
		return ddg;
	}
	
	public String getLocation()
	{
		return astRoot.getLocationString();
	}

	public String getSignature()
	{
		return signature;
	}
	
	private void setSignature(FunctionDef node)
	{
		signature = node.getFunctionSignature();
	}

}
