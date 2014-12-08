import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ProgressInputStream extends FilterInputStream {

	private final double maxbytes;
    private long current = 0;
    
    /* Progress bar에서 현재 진행률에 대해 알 수 있는 값이 없기 때문에 만든 클래스
     * 파일의 총길이를 객체생성 할 때 받아오고, 현재 얼만큼 읽었는지 읽을 때 마다 byte단위로 counting한다. */
	protected ProgressInputStream(InputStream paramInputStream, long bytesexpect) {
		super(paramInputStream);
		// TODO Auto-generated constructor stub
		maxbytes = (double)bytesexpect;
	}
	
	/* 현재까지 읽은 Byte / 파일 총 길이(Byte) */
	public double getProgress(){
		return current / maxbytes;
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		final int ret = super.read();
		if (ret >= 0) {
			current++;
		}
		return ret;
	}

	@Override
	public int read(byte[] paramArrayOfByte) throws IOException {
		// TODO Auto-generated method stub
		final int ret = super.read(paramArrayOfByte);
		current += ret;
		return ret;
	}

	@Override
	public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
			throws IOException {
		// TODO Auto-generated method stub
		final int ret = super.read(paramArrayOfByte, paramInt1, paramInt2);
		current += ret;
		return ret;
	}

	@Override
	public long skip(long paramLong) throws IOException {
		// TODO Auto-generated method stub
		final long ret = super.skip(paramLong);
		current += ret;
		return ret;
	}
}
