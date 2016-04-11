package Service;

public class TEST {
	public static void main(String[] args) {
		byte[] pwd = ServiceMethods.getPwdHash("beautifullove");
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<pwd.length; i++) {
			sb.append(Integer.toString((pwd[i] & 0xff) + 0x100, 16).substring(1));
		}
		System.out.println(sb.toString());
	}
}
