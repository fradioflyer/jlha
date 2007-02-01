/*
 * $RCSfile: BitCutter.java,v $ $Date: 2001/11/23 10:06:49 $ 
 * Ver. $Name:  $  Source revision. $Revision: 1.4 $
 *
 * Copyright 2000 by TURNER.
 */

package jp.gr.java_conf.turner.util.lha;

import java.io.*;

/**
 * 任意のビット数のデータを切り出せる入力ストリーム
 *
 * @author		TURNER
 */
class BitCutter extends InputStream{

	protected InputStream is;

	protected static final int BYTE_BITS = 8;
	protected int bitbuf = 0;
	protected int bitbuf_count = 0;
	protected int eof_count = 0;
	protected long read_counter = 0;

	protected static final int CUT_MAX = 32 - (BYTE_BITS) + 1;

	protected BitCutter( InputStream arg_is ){
		is = arg_is;
	}

	protected BitCutter( RandomAccessFile raf )
		throws IOException{
		is = new FileInputStream( raf.getFD() );
	}
	
	/**
	 *  バイトストリームから任意のビット数スキップする.
	 * 
	 * @arg	n		読み飛ばすデータのビット数
	 * @author		TURNER
	 */
	protected void skipBits( int n )
		throws IOException
	{
		if( n < 0 ){
			throw new IllegalArgumentException("arg="+n+" must be plus.");
		}

		if( n <= bitbuf_count ){
			bitbuf_count -= n;
		}
		else{
			n -= bitbuf_count;
			bitbuf_count = 0;
			if( n >= BYTE_BITS ){
				int skip_bytes = n / BYTE_BITS;
				if( skip_bytes > is.skip( skip_bytes ) ){
					throw new EOFException("Unexpected end of LHA input stream.");
				}
				read_counter += skip_bytes;
				n = n % BYTE_BITS;
			}
			if( n > 0 ){
				readByte();
				bitbuf_count -= n;
				if( bitbuf_count < eof_count ){
					throw new EOFException("Unexpected end of LHA input stream.");
				}
			}
		}
	}

	/**
	 *  １バイトに満たない半端なビットを捨ててさらに任意のバイト数スキップする.
	 * 
	 * @arg	bytes	読み飛ばすバイト数
	 * @author		TURNER
	 */
	public long skip( long bytes )
		throws IOException
	{
		if( bytes < 0 ){
			throw new IllegalArgumentException("arg="+bytes+" must be plus.");
		}
		long ret = 0;
		long ret_wk;

		adjustByteAlignment();
		while( bitbuf_count > 0 && bytes > 0 && this.read() != -1 ){
			ret++;
			bytes--;
		}

		ret_wk = is.skip( bytes );
		if( ret_wk != -1 ){
			ret += ret_wk;
			read_counter += ret_wk;
		}

		if( ret > 0 || bytes != 0 ){
			return ret;
		}
		else{
			return -1;
		}
	}
	

	/**
	 *  １バイトに満たない半端なビットを捨てたのち、１バイト読み込む.
	 * 
	 * @return	読み込んだデータ（ファイルの終わりに達していれば-1)
	 * @author		TURNER
	 */
	public int read()
		throws IOException
	{
		adjustByteAlignment();
		try{
			return getBits(BYTE_BITS);
		}
		catch( EOFException e ){
			return -1;
		}
	}

	/**
	 *  １バイトに満たない半端なビットを捨てたのち、複数バイト読み込む.
	 * 
	 * @return	読み込めたデータのバイト数
	 * @author		TURNER
	 */
	public int read( byte[] b, int off, int len )
		throws IOException
	{
		int tail = off+len;
		int i = off;
		adjustByteAlignment();
		try{
			while( bitbuf_count > 0 ){
				b[i] = (byte)getBits(BYTE_BITS);
				i++;
			}
		}catch( EOFException e ){
			if( i > off ){
				return ( i - off );
			}else{
				return -1;
			}
		}
		if( i < tail ){
			int let = is.read( b, i, tail - i );
			if( let > 0 ){
				i += let;
				read_counter += let;
			}
		}
		if( i > off || len == 0 ){
			return ( i - off );
		}else{
			return -1;
		}
	}

	/**
	 *  １バイトに満たない半端なビットを捨てたのち、複数バイト読み込む.
	 * 
	 * @return	読み込めたデータのバイト数
	 * @author		TURNER
	 */
	public int read( byte[] b )
		throws IOException
	{
		return this.read( b, 0, b.length );
	}

	/**
	 *  バッファに残っている１バイトに満たない半端なビットを捨てる.
	 * 
	 * @author		TURNER
	 */
	protected void adjustByteAlignment()
	{
		bitbuf_count -= (bitbuf_count % BYTE_BITS);
	}

	/**
	 *  ストリームから任意のビット数のデータをポインタは進めずコピーする.
	 * 
	 * @arg	n		コピーするデータのビット数(最大=CUT_MAX)
	 * @return		コピーした値
	 * @author		TURNER
	 */
	protected int copyBits( int n )
		throws IOException
	{
		if( n < 0 || n > CUT_MAX ){
			throw new 
				IllegalArgumentException("arg="+n+" limit:0<=arg<="+CUT_MAX);
		}

		while( n > bitbuf_count ){
			readByte();
		}

		if( bitbuf_count <= eof_count ){
			throw new EOFException("Unexpected end of LHA input stream.");
		}
		int mask = ( 1 << n ) - 1;
		int ret = bitbuf >>> (bitbuf_count - n);
		return ( ret & mask );
	}

	/**
	 *  バイトストリームから任意のビット数のデータを切り出す.
	 * 
	 * @arg	n		取り出すデータのビット数(最大=CUT_MAX)
	 * @return		取り出した値
	 * @author		TURNER
	 */
	protected int getBits( int n )
		throws IOException
	{
		if( n < 0 || n > CUT_MAX ){
			throw new 
				IllegalArgumentException("arg="+n+" limit:0<=arg<="+CUT_MAX);
		}

		while( n > bitbuf_count ){
			readByte();
		}

		if( (bitbuf_count - n) < eof_count ){
			throw new EOFException("Unexpected end of LHA input stream.");
		}
		int mask = ( 1 << n ) - 1;
		int ret = bitbuf >>> (bitbuf_count - n);
		bitbuf_count -= n;
		return ( ret & mask );
	}

	/**
	 *  バイトストリームから1ビット数のデータを切り出す.
	 * 
	 * @return		取り出した値（０ｏｒ１）
	 * @author		TURNER
	 */
	protected int getBit()
		throws IOException
	{
		if( 0 == bitbuf_count ){
			readByte();
		}

		int ret = bitbuf >>> (bitbuf_count - 1);
		bitbuf_count --;
		if( bitbuf_count < eof_count ){
			throw new EOFException("Unexpected end of LHA input stream.");
		}
		return ( ret & 1 );
	}

	/**
	 * 連続したビットの並びの数を数えます.
	 * 引数と違うビットが最初に現れたところまで読み込まれた扱いになります。
	 *
	 * @check_bit	1なら連続した１の数０なら連続した０の数を数えます。
	 * @return		連続した数(いきなり引数と違うビットなら０を返す）
	 * @author		TURNER
	 */
	protected int getClusterLen(int check_bit)
		throws IOException
	{
		int ret;
		int mask;
		boolean check_bool;

		if( (check_bit & ~(1)) != 0 ){
			throw new IllegalArgumentException( "bit=0or1" );
		}

		check_bool = (check_bit==1) ? true : false;

		if( bitbuf_count == 0 ){
			readByte();
			mask = 1 << (BYTE_BITS - 1);
		}
		else{
			mask = 1 << (bitbuf_count - 1);
		}
		ret = 0;
		bitbuf_count--;
		if( bitbuf_count < eof_count ){
			throw new EOFException("Unexpected end of LHA input stream.");
		}
		while( ((bitbuf & mask) == 0) ^ check_bool ){
			if( bitbuf_count == 0 ){
				readByte();
				mask = 1 << (BYTE_BITS - 1);
			}
			else{
				mask >>>= 1;
			}
			ret++;
			bitbuf_count--;
			if( bitbuf_count < eof_count ){
				throw new EOFException("Unexpected end of LHA input stream.");
			}
		}

		return ret;
	}

	/**
	 * ガンマ符号を取り出す.
	 *
	 * @return		ガンマ符号を復号した結果。
	 */
	protected int getGamma()
		throws IOException
	{
		int len0 = getClusterLen(0);
		int bit = getBits(len0);
		int code = (1 << len0) | bit;
		return code;
	}
	
	public void close()
		throws IOException
	{
		is.close();
	}

	/**
	 * １バイト読み込み.
	 */
	private void readByte()
		throws IOException
	{
		int c = is.read();
		if( c == -1 ){
			eof_count += BYTE_BITS;
			c = 0;
		}else{
			read_counter++;
		}
		bitbuf <<= BYTE_BITS;
		bitbuf |= (0xFF & c); 
		bitbuf_count += BYTE_BITS;
	}
	

	/**
	 * カウンター値取得.
	 */
	private long getReadCounterValue()
	{
		return (read_counter - (bitbuf_count/BYTE_BITS));
	}

	/**
	 * カウンターリセット.
	 */
	private void resetReadCounter()
	{
		read_counter = (bitbuf_count/BYTE_BITS);
	}
	
}
