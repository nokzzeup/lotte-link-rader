public class Notice {
    private int id;
    private String tag;
    private String title;
    private String content;
    private String date;
    private boolean unread;

    public Notice(int id, String tag, String title, String content, String date) {
        this.id      = id;
        this.tag     = tag;
        this.title   = title;
        this.content = content;
        this.date    = date;
        this.unread  = true;
    }

    public int     getId()      { return id; }
    public String  getTag()     { return tag; }
    public String  getTitle()   { return title; }
    public String  getContent() { return content; }
    public String  getDate()    { return date; }
    public boolean isUnread()   { return unread; }

    public void setTag(String tag)         { this.tag = tag; }
    public void setTitle(String title)     { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setUnread(boolean unread)  { this.unread = unread; }

    // 파일 저장용 (한 줄로 변환)
    public String toFileLine() {
        return id + "|" + tag + "|" + title.replace("|", "／")
                + "|" + content.replace("|", "／").replace("\n", "\\n")
                + "|" + date + "|" + unread;
    }

    // 파일에서 읽기 (한 줄 → 객체)
    public static Notice fromFileLine(String line) {
        try {
            String[] p = line.split("\\|", 6);
            if (p.length < 6) return null;
            int     id      = Integer.parseInt(p[0]);
            String  tag     = p[1];
            String  title   = p[2].replace("／", "|");
            String  content = p[3].replace("／", "|").replace("\\n", "\n");
            String  date    = p[4];
            boolean unread  = Boolean.parseBoolean(p[5]);
            Notice n = new Notice(id, tag, title, content, date);
            n.setUnread(unread);
            return n;
        } catch (Exception e) {
            return null;
        }
    }
}