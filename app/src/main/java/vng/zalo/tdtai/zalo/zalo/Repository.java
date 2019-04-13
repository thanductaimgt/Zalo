package vng.zalo.tdtai.zalo.zalo;

public class Repository {
    private static volatile Repository INSTANCE;

    private Repository(){}

    public static Repository getInstance(){
        if(INSTANCE == null){
            synchronized (Repository.class){
                if(INSTANCE == null){
                    INSTANCE = new Repository();
                }
            }
        }
        return null;
    }
}
