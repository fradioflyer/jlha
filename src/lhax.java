

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.gr.java_conf.turner.util.lha.LhaEntry;
import jp.gr.java_conf.turner.util.lha.LhaInputStream;

/**
 * Lhaライブラリ動作テストクラス
 *
 * @author TURNER
 */
public class lhax
{
	static boolean header_view = false;
	static boolean silent = false;

	public static void main(String[] args)
		throws IOException
	{
		File lhaFile = new File( args[0] );
		if( args.length >= 2 && args[1].equals("v") ){
			header_view = true;
		}
		if( args.length >= 2 && args[1].equals("s") ){
			silent = true;
		}
		extractLha( lhaFile );
	}

	private static void extractLha( File lhaFile )
		throws IOException
	{
		LhaInputStream lis = null;
		long totalTime = 0;
		try{
			lis = new LhaInputStream( new FileInputStream(lhaFile) );
			System.out.println("====================================================");
			System.out.println("LHA file         =\"" + lhaFile.getName() + "\"" );
			System.out.println("====================================================");

			LhaEntry ent;
			byte[] buffer = new byte[256];
			while( ( ent = lis.getNextEntry() ) != null ){
				if( !silent ){
					System.out.println("----------------------------------------------------");
					System.out.println( "filename         =\""+ent.getName() + "\"" );
					System.out.println("----------------------------------------------------");
					if( header_view ){
						System.out.println( "isDiredtory      ="+ent.isDirectory() );
						System.out.println( "getDir           =\"" + ent.getDir() + "\"" );
						System.out.println( "getCompressMethod=\""+ent.getCompressMethod() + "\"" );
						System.out.println( "getCompressedSize="+ent.getCompressedSize() );
						System.out.println( "getSize          ="+ent.getSize() );
						System.out.println( "getDate          ="+new java.util.Date( ent.getDate() ) );
						String wk = "0000000"+Integer.toBinaryString( ent.getAttribute() );
						System.out.println( "getAttribute     ="+ wk.substring( wk.length() - 8 ) );
						System.out.println( "getHeaderLevel   ="+ent.getHeaderLevel() );
						System.out.println( "getCRC           ="+Integer.toHexString( ent.getCRC() ) );
						System.out.println( "getOSType        =\'"+ent.getOSType() + "\'" );

						System.out.println( "getComment       =\"" + ent.getComment() + "\"" );
						System.out.print( "getExtra         =[");
						for( int i=0; i<ent.getExtra().length; i++ ){
							if( i > 0 )System.out.print(" ");
							System.out.print( Integer.toHexString( 0xFF & (int)ent.getExtra()[i] ) );
						}
						System.out.println("]");
						System.out.println("----------------------------------------------------");
					}
				}
	            String outname = ent.getName();
	            String outDir = ent.getDir();
	            if( outDir != null && outDir.length() > 0 ){
					( new File( outDir ) ).mkdirs();
				}
	            File decodeFile = new File( outDir, outname );
				FileOutputStream os = null;
				try{
		            os = new FileOutputStream( decodeFile );

					int len = 0;
					int size = 0;
					long currentTime = System.currentTimeMillis();
					while( (len = lis.read( buffer )) != -1 ){
		                os.write(buffer,0,len);
						if( !silent ){
							if( size % (buffer.length * 8 * 50) == 0 )
								System.out.println();
							if( size % (buffer.length * 8) == 0 )
								System.out.print("o");
						}
						size += len;
					}
					currentTime = System.currentTimeMillis() - currentTime;
					System.out.println( "\nmilliSec:"+currentTime );
					totalTime += currentTime;
				}
				catch( IOException e ){
					e.printStackTrace();
				}
				finally{
					if( os != null ){
						os.close();
					}
				}
				System.out.println();
			}
			System.out.println("=====================  END  ========================");
			System.out.println( "totalTime(Milli):"+totalTime);
		}catch( IOException e ){
			System.out.println( "\n" + e.getMessage() );
			e.printStackTrace();
		}finally{
			lis.close();
		}
	}

}
