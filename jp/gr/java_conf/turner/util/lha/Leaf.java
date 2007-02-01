/*
 * $RCSfile: Leaf.java,v $ $Date: 2000/05/04 15:43:22 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

/**
 * ハフマンツリー用の葉（末端ノード）.
 */
class Leaf extends TreeNode
{
	int code = 0;       //ハフマン符号			(StaticHuffmanで使用)
	int code_len = 0;   //ハフマン符号のビット数(StaticHuffmanで使用)
	int real_code;      //本来のコード
}
