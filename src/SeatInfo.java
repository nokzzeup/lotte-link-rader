public class SeatInfo {
    private String name;
    private int remain;
    private String status;

    public SeatInfo(String name, int remain) {
        this.name = name;
        this.remain = remain;
        if (remain == 0) this.status = "매진";
        else if (remain < 30) this.status = "임박";
        else this.status = "여유";
    }

    public String getName() { return name; }
    public int getRemain() { return remain; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        if (remain == 0) return "  [매진] " + name;
        else if (remain < 30) return "  [" + remain + "석] " + name + " ★ 임박!";
        else return "  [" + remain + "석] " + name;
    }
}