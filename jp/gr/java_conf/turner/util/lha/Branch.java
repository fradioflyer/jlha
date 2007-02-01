/*
 * $RCSfile: Branch.java,v $ $Date: 2000/05/04 15:43:22 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

/**
 * ハフマンツリー用の分岐ノード.
 */
class Branch extends TreeNode
{
	TreeNode child_0 = null;	//0の枝
	TreeNode child_1 = null;	//1の枝
}

