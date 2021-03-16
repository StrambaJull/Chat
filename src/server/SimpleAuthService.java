package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{

    private class UserData{
        String login;
        String password;
        String nickName;

        public UserData (String login, String password, String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }
    }

    List<UserData> users;
    public SimpleAuthService(){
        users = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
        }
        users.add(new UserData("qwe", "qwe", "qwe"));
        users.add(new UserData("asd", "asd", "asd"));
        users.add(new UserData("zxc", "zxc", "zxc"));
    }


    @Override
    public String getNickByLoginAndPassword (String login, String password) {
        for(UserData user: users){
            if(user.login.equalsIgnoreCase(login) && user.password.equalsIgnoreCase(password)){
                return user.nickName;
            };
        }
        return null;
    }

    @Override
    public boolean registration (String login, String password, String nickName) {
        for(UserData user: users){
            if(user.login.equalsIgnoreCase (login) || user.nickName.equalsIgnoreCase (nickName)){ //проверим на наличие такого же логина+никнейма. Если такие логин+никнейм уже есть, то
                return false; //не регистрируем такого пользователя
            }
        }
        users.add(new UserData(login, password, nickName)); //Если такого логина+никнейма нет, то добавляем нового клиента в список
        return true;
    }

}
