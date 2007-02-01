/*
 * $RCSfile: LhaInputStream.java,v $ $Date: 2001/04/10 18:01:37 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.6 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;

/**
 * LHAファイルデコーダストリームクラス.
 * 対応メソッド -lh0-, -lh1-, -lh4-,-lh5-,-lh6- ,-lh7-
 *
 * @author		TURNER
 * @version 	0.2
 */
public class LhaInputStream extends FilterInputStream
{

	private LhaEntry lha_entry;			//LHAエントリクラス
	private CRC16 crc = new CRC16();
	private byte[] tmpbuf = new byte[256];

	private long remaining; 			//展開後のデータののこりの長さ。

	private boolean closed = false;
	private LZHDecoder lzhDecoder;		//肝心要のデコーダクラス

	// エントリーが最後に達しているかどうかのフラグ。
	private boolean lha_entryEOF = false;

	
	static final int CMP_TYPE_LH0 = 0;
	static final int CMP_TYPE_LH1 = 1;
	static final int CMP_TYPE_LH2 = 2;
	static final int CMP_TYPE_LH3 = 3;
	static final int CMP_TYPE_LH4 = 4;
	static final int CMP_TYPE_LH5 = 5;
	static final int CMP_TYPE_LH6 = 6;
	static final int CMP_TYPE_LH7 = 7;
	private int cmp_type;

	/**
	 * ストリームがcloseしていないかチェックする.
	 * 
	 * @exception java.io.IOException IOエラーが起きたとき
	 */
	private void ensureOpen()
		throws IOException
	{
		if (closed) {
			throw new LhaException("Stream closed");
		}
	}

	/**
	 * ＬＨＡデコーダストリームのコンストラクタ.
	 * 
	 * @param in ＬＨＡファイルフォーマットの入力ストリーム
	 */
	public LhaInputStream(InputStream in)
	{
		super( new BitCutter( in ) );
		if(in == null) {
			throw new NullPointerException("InputStream in null");
		}
	}

	/**
	 * LHAファイルエントリを読み込み、エントリデータの最初にストリームを配置します.
	 * <p>
	 * ファイルエントリはアーカイブ内の１ファイルを表しています。
	 * この関数を呼び出すことによりアーカイブ内の１つのデータをストリームとして
	 * 読み込みできるようになります。
	 * <p>
	 * また、１つのエントリでEOFに達してもさらにエントリが続いている限り、
	 * この関数を呼び出して次のエントリデータの最初にストリームを配置できます。
	 * <p>
	 * アーカイブの最後に達したときはnullを返します。
	 *
	 * @return	ファイルエントリがまだあれば次のLhaEntryオブジェクト
	 * @exception java.io.IOException IOエラーが起きたとき
	 */
	public synchronized LhaEntry getNextEntry() 
		throws IOException 
	{
		ensureOpen();
		if (lha_entry != null) {
			closeEntry();
		}
		crc.reset();

		if ((lha_entry = readHeader()) == null) {
			return null;
		}
		remaining = lha_entry.getSize();
		lha_entryEOF = false;
		if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH0) ){
			cmp_type = CMP_TYPE_LH0;
		}else if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH1) ){
			cmp_type = CMP_TYPE_LH1;
   			lzhDecoder = new LZHDecoder( CMP_TYPE_LH1, (BitCutter)in );
		}else if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH4) ){
			cmp_type = CMP_TYPE_LH4;
   			lzhDecoder = new LZHDecoder( CMP_TYPE_LH4, (BitCutter)in );
		}else if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH5) ){
			cmp_type = CMP_TYPE_LH5;
   			lzhDecoder = new LZHDecoder( CMP_TYPE_LH5, (BitCutter)in );
		}else if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH6) ){
			cmp_type = CMP_TYPE_LH6;
   			lzhDecoder = new LZHDecoder( CMP_TYPE_LH6, (BitCutter)in );
		}else if( lha_entry.getCompressMethod().equals(LhaEntry.METHOD_LH7) ){
			cmp_type = CMP_TYPE_LH7;
   			lzhDecoder = new LZHDecoder( CMP_TYPE_LH7, (BitCutter)in );
		}else{
			throw new LhaException("unsupported method:"+lha_entry.getCompressMethod());
		}
		return lha_entry;
	}

	/**
	 * 現在のLHAエントリーをクローズし、次のエントリの直前までスキップします.
	 *
	 * @exception java.io.IOException IOエラーが起きたとき
	 */
	public synchronized void closeEntry()
		throws IOException
	{
		ensureOpen();
		while (read(tmpbuf, 0, tmpbuf.length) != -1) ;
		if( lha_entry.getCRC() != crc.getValue() ){
			throw new LhaException( "CRC check error. at:"+lha_entry.getName() );
		}
		lha_entryEOF = true;
	}

	/**
	 * 現在の入力データが EOF に達したあとで呼び出した場合は 0 を返します,
	 * そうでない場合は常に 1 を返します.
	 * <p>
	 * 本来InputStreamのavailable()関数はブロックせずに読みこみ可能な
	 * バイト数を返すためのものです。
	 * <p>
	 * ですがブロックなしで読み込める実際のバイト数は展開してみないとわからないので
	 * ここではとりあえずこのような値を返します。(ZipInputStreamの仕様を真似ています)
	 * 
	 * @return	   現在の入力データが EOF に達していない場合は常に 1.
	 * @exception java.io.IOException
	 */
	public synchronized int available() 
		throws IOException 
	{
		ensureOpen();
		if (lha_entryEOF) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * 読み込み中のLHAエントリから１バイト読み込みます.
	 *
	 * @return 読み込まれた値
	 * 		EOFに達していたときは-1を返す。
	 * @exception java.io.IOException IOエラーが起きたとき
	 */
	public synchronized int read()
		throws IOException
	{
		int result = 0;
		
		ensureOpen();
		if ( lha_entryEOF ) {
			return -1;
		}
		if ( remaining <= 0 ) {
			lha_entryEOF = true;
			return -1;
		}
		
		switch( cmp_type )
		{
		case CMP_TYPE_LH0:
			result = in.read();
			break;
		case CMP_TYPE_LH1:
		case CMP_TYPE_LH4:
		case CMP_TYPE_LH5:
		case CMP_TYPE_LH6:
		case CMP_TYPE_LH7:
			result = lzhDecoder.read();
			break;
		default:
			throw new InternalError();
		}

		if(result == -1){
			throw new EOFException("Unexpected end of LHA input stream.");
		}

		crc.update( (byte)result );
		remaining --;
		if( remaining == 0 ){
			lha_entryEOF = true;
		}
		return result;
	}

	/**
	 * 読み込み中のエントリからデータを読み込みます.
	 * 指定されたbyte配列に
	 * 配列の大きさぶんまで可能な限り読み込みます。
	 *
	 * @param b 読み込まれたデータを格納するためのbyte型の配列
	 * @return 読み込まれた有効なバイト数。またエントリの
	 * 				EOFに達していたときは-1を返す。
	 * @exception java.io.IOException IOエラーが起きたとき 
	 */
	public synchronized int read( byte[] b )
		throws IOException
	{
		return read( b, 0, b.length );
	}

	/**
	 * 読み込み中のエントリからデータを読み込みます.
	 * 引数で指定されたぶんまで
	 * 可能な限りbyteの配列に読み込みます。
	 *
	 * @param b 読み込まれたデータを格納するためのbyte型の配列
	 * @param off 配列上の読み込み開始インデックス。
	 * @param len 最大読み込みバイト数
	 * @return 読み込まれた有効なバイト数。またエントリの
	 * 				EOFに達していたときは-1を返す。
	 * @exception java.io.IOException IOエラーが起きたとき 
	 */
	public synchronized int read(byte[] b, int off, int len)
		throws IOException
	{
		if( lha_entryEOF ){
			return -1;
		}
		if ( len >= remaining ) {
			lha_entryEOF = true;
			len -= (int)(len - remaining);
		}

		switch( cmp_type )
		{
		case CMP_TYPE_LH0:
			in.read( b, off, len );
			break;
		case CMP_TYPE_LH1:
		case CMP_TYPE_LH4:
		case CMP_TYPE_LH5:
		case CMP_TYPE_LH6:
		case CMP_TYPE_LH7:
			lzhDecoder.read( b, off, len );
			break;
		default:
			throw new InternalError();
		}

		crc.update( b, off,len );
		remaining -= len;
		return len;
	}

	/**
	 * 現在読み込み中のストリームを引数で指定したバイト数だけスキップします.
	 * EOFに達した場合はそこで停止します.
	 * @param n スキップする数
	 * @return 実際にスキップした数
	 * @exception java.io.IOException IOエラーが起きたとき 
	 * @exception IllegalArgumentException	n < 0 のとき
	 */
	public synchronized long skip(long n) throws IOException {
		if (n < 0) {
			throw new IllegalArgumentException("negative skip length");
		}
		ensureOpen();

		long total = 0;
		long len;
		while (total < n) {
			len = n - total;
			if (len > tmpbuf.length) {
				len = tmpbuf.length;
			}
			len = read(tmpbuf, 0, (int)len);
			if (len == -1) {
				lha_entryEOF = true;
				break;
			}
			total += len;
		}
		return total;
	}

	/**
	 * LHAInputStreamをクローズします.
	 * エントリではなくデータの源のストリームをクローズします.
	 * @exception java.io.IOException IOエラーが起きたとき
	 */
	public synchronized void close() throws IOException {
		in.close();
		closed = true;
	}

	/*
	 * 次のエントリのLHAファイルヘッダを読み込みます。
	 */
	private LhaEntry readHeader()
		throws IOException
	{
		LhaEntry e = null;
		e = new LhaEntry();
		if( e.loadFrom( in ) == true ){
			return e;
		}else{
			return null;
		}
	}

	/**
	 * 新しい <code>LhaEntry</code> オブジェクトをファイル名を指定して
	 * 生成します.
	 *
	 * @param name the LHA file entry name
	 */
	protected LhaEntry createLhaEntry(String name) {
		return new LhaEntry(name);
	}

}
