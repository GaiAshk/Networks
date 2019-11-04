
public class hashit{

    public static void main(String[] args) {
        String s = "slim";

        while (s.length() < 16) s += s;
        byte[] sbytes = null;
        try { sbytes = s.getBytes("US-ASCII");
        } catch(Exception e) {
            System.out.println("illegal key string");
            System.exit(1);
        }
        int i = 0;
        int h = 0x37ace45d;
        while (i+1 < sbytes.length) {
            int x = (sbytes[i] << 8) | sbytes[i+1];
            h *= x;
            int top = h & 0xffff0000;
            int bot = h & 0xffff;
            h = top | (bot ^ ((top >> 16)&0xffff));
            i += 2;
        }
        if (h < 0) h = -(h+1);
        System.out.println((double) h/Integer.MAX_VALUE);
        System.out.println(h);

    }

}
