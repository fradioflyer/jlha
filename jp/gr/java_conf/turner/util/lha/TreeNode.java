/*
 * $RCSfile: TreeNode.java,v $ $Date: 2000/05/04 15:43:23 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.2 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

/**
 * ハフマンツリー用のノード.
 */
abstract class TreeNode
{
	Branch parent = null;//親(DynamicHuffmanで使用)
	int freq = 0;       //ハフマン符号の頻度カウンタ(DynamicHuffmanで使用)
	int index = 0;      //頻度順でソートされた時のインデックス
	                    //↑(DynamicHuffmanで使用)
}
