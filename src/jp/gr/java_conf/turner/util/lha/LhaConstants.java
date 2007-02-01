/*
 * $RCSfile: LhaConstants.java,v $ $Date: 2000/04/15 17:28:07 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.1.1.1 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

/**
 * LHAの各種定数を定義します。
 *
 * @version 	0.1, 2000/03/13
 */
public interface LhaConstants{

	/** 
	 * リードオンリー属性値用マスク.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_RDONLY= 1;

	/** 
	 * 隠しファイル属性値用マスク.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_HIDDEN= 2;

	/** 
	 * システムファイル属性値用マスク.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_SYSTEM= 4;


	/** 
	 * ボリュームラベル属性値用マスク（多分使わない？）.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_LABEL = 8;

	/** 
	 * ディレクトリ属性値用マスク.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_DIREC =16;

	/** 
	 * アーカイブフラグ属性値用マスク.
	 * @see LhaEntry#getAttribute() 
	 * @see LhaEntry#setAttribute(byte)
	 */
	public static final byte FA_ARCH  =32;

	/*
	 *OSTYPE定数については以下のサイトの情報を元にしています。
	 *（Dolphin's ホームページ）
	 * http://www2m.biglobe.ne.jp/~dolphin/
	 *
	 * 公式に予約されているもの 
	 * ここでいう公式とは、LHAの開発者である吉崎氏が
	 * CMAGAZINE1991/1の記事で公表したものという意味だそう。
	 */

	/** 
	 * OSタイプMS-DOS.
	 * <p>値='M'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_MSDOS = 'M';
	/** 
	 * OSタイプOS/2.
	 * <p>値='2'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_OS2   = '2';
	/** 
	 * OSタイプOS-9.(MacOSではない).
	 * <p>値='9'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_OS9   = '9';
	/** 
	 * OSタイプOS68K.
	 * <p>値='K'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_OS68K = 'K';
	/** 
	 * OSタイプOS386.
	 * <p>値='3'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_OS386 = '3';
	/** 
	 * OSタイプHUMAN68K.
	 * <p>値='H'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_HUMAN = 'H';
	/** 
	 * OSタイプUNIX.
	 * <p>値='U'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_UNIX  = 'U';
	/** 
	 * OSタイプCP/M.
	 * <p>値='C'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_CPM   = 'C';
	/** 
	 * OSタイプFLEX.
	 * <p>値='F'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_FLEX  = 'F';
	/** 
	 * OSタイプMacOS.
	 * <p>値='m'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_MAC   = 'm';
	/** 
	 * OSタイプRUNSER.
	 * <p>値='R'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_RUNSER= 'R';

	/** 
	 * OSタイプWindowsNT. <p>
	 * 非公式.(吉崎氏がCMAGAZINE1991/1の記事で書いたものでないという意味)
	 * <p>OSTYPE_MSDOSが使われることが多い。<p>
	 * <p>値='W'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_NT    = 'W';
	/** 
	 * OSタイプWindows95. <p>
	 * 非公式.(吉崎氏がCMAGAZINE1991/1の記事で書いたものでないという意味)
	 * <p>OSTYPE_MSDOSが使われることが多い。<p>
	 * <p>値='w'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_95    = 'w';
	/** 
	 * OSタイプTOWNSOS. <p>
	 * 非公式.(吉崎氏がCMAGAZINE1991/1の記事で書いたものでないという意味)
	 * <p>値='T'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_TOWNSOS='T';  
	/** 
	 * OSタイプXOSK. <p>
	 * 非公式.(吉崎氏がCMAGAZINE1991/1の記事で書いたものでないという意味)
	 * <p>値='X'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_XOSK  = 'X';  

	/** 
	 * OSタイプGENERIC(汎用/無指定). <p>
	 * 書き込み時のOSTYPEはこの値がデフォルト(予定).<p>
	 * <p>値='\0'<p>
	 * @see LhaEntry#getOSType() 
	 * @see LhaEntry#setOSType(char)
	 */
	public static final char OSTYPE_GENERIC = '\0';

	/** 
	 * 圧縮形式lh0(無圧縮). <p>
	 * 値="-lh0-"<p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH0 = "-lh0-";

	/** 
	 * 圧縮形式lh1. <p>
	 * 値="-lh1-"<p>
     * 4k sliding dictionary(max 60 bytes) + dynamic Huffman
     * + fixed encoding of position.
     *
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH1 = "-lh1-";

	/** 
	 * 圧縮形式lh2. <p>
	 * 値="-lh2-"<p>
	 * 8k sliding dictionary(max 256 bytes) + dynamic Huffman
	 * <p>
	 * <STRONG>現在未サポート</STRONG>
	 * <p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH2 = "-lh2-";

	/** 
	 * 圧縮形式lh3. <p>
	 * 値="-lh3-"<p>
	 * 8k sliding dictionary(max 256 bytes) + static Huffman
	 + <p>
	 * <STRONG>現在未サポート</STRONG>
	 * <p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH3 = "-lh3-";

	/** 
	 * 圧縮形式lh4. <p>
	 * 値="-lh4-"<p>
	 * 4k sliding dictionary(max 256 bytes) + static Huffman
	 *  + improved encoding of position and trees
	 *
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH4 = "-lh4-";
              

	/** 
	 * 圧縮形式lh5. <p>
	 * 値="-lh5-"<p>
     * 8k sliding dictionary(max 256 bytes) + static Huffman
     *         + improved encoding of position and trees
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH5 = "-lh5-";

	/** 
	 * 圧縮形式lh6. <p>
	 * 値="-lh6-"<p>
     *  32k sliding dictionary(max 256 bytes) + static Huffman
	 *  + improved encoding of position and trees
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH6 = "-lh6-";

	/** 
	 * 圧縮形式lh7. <p>
	 * 値="-lh7-"<p>
     *  64k sliding dictionary(max 256 bytes) + static Huffman
	 *	  + improved encoding of position and trees.
     *
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LH7 = "-lh7-";

	/** 
	 * 圧縮形式lzs. <p>
	 *"-lzs-"<p>
	 * 2k sliding dictionary(max 17 bytes)
	 * <p>
	 * <STRONG>現在未サポート</STRONG>
	 * <p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LZS = "-lzs-";

	/** 
	 * 圧縮形式lz4(無圧縮). <p>
	 * 値="-lz4-"<p>
	 * no compression
	 * <p>
	 * <STRONG>現在未サポート</STRONG>
	 * <p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LZ4 = "-lz4-";

	/** 
	 * 圧縮形式lz5. <p>
	 * 値="-lz5-"<p>
	 * 4k sliding dictionary(max 17 bytes)
	 * <p>
	 * <STRONG>現在未サポート</STRONG>
	 * <p>
	 * @see LhaEntry#getCompressMethod() 
	 * @see LhaEntry#setCompressMethod(String)
	 */
	public static final String METHOD_LZ5 = "-lz5-";

}
