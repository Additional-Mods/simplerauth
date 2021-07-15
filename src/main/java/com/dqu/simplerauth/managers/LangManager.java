package com.dqu.simplerauth.managers;

import net.minecraft.text.LiteralText;

import java.util.HashMap;

public class LangManager {
    private static final HashMap<String, String> lang = new HashMap<String, String>();

    public static LiteralText getLiteralText(String key) {
        return new LiteralText(get(key));
    }

    public static String get(String key) {
        return lang.get(key);
    }

    public static void loadTranslations(String language) {
        /*
            Temporary.
            Until Fabric adds server-side languages support.
            https://github.com/FabricMC/fabric/pull/1501
         */
        lang.clear();
        if (language == "ru") {
            lang.put("command.login.notregistered", "§cВы не зарегестрированы! Используйте §f/register");
            lang.put("command.login.wrongpassword", "§cНеверный пароль!");
            lang.put("command.login.alreadylogged", "§cВы уже вошли!");
            lang.put("command.register.alreadyregistered", "§cВы уже зарегестрированы! Используйте §f/login");
            lang.put("command.register.globaltype", "§cНа сервере установлен глобальный пароль, регистрация невозможна! Используйте §f/login");
            lang.put("command.changepassword.notregistered", "§cВы не зарегестрированы!");
            lang.put("command.changepassword.success", "§aВы успешно изменили пароль!");
            lang.put("command.onlineauth.globaltype", "§cGlobal password is set on this server, online auth is not possible! Use §f/login");
            lang.put("command.onlineauth.notregistered", "§cYou are not registered! Use §f/register §cbefore enabling online auth.");
            lang.put("command.onlineauth.featuredisabled", "§cOptional online auth is not enabled in this server!");
            lang.put("command.onlineauth.warning", "§cYou will lose all your data linked to your UUID, like your inventory & ender chest, statistics, advancements, villager discounts, etc.");
            lang.put("command.onlineauth.cannotenable", "§cAn official account with this username does not exist! You can't enable this feature");
            lang.put("command.onlineauth.confirmenable", "§6To confirm, use §f/onlineauth enable <password>§6. You can disable it with §f/onlineauth disable§6.");
            lang.put("command.onlineauth.alreadyenabled", "§cAlready enabled!.");
            lang.put("command.onlineauth.enabled", "§aOnline auth has been enabled for your account. Reconnect to the server to apply the changes.");
            lang.put("command.onlineauth.confirmdisable", "§6To confirm, use §f/onlineauth disable <password>§6. You can enable it again with §f/onlineauth§6.");
            lang.put("command.onlineauth.alreadydisabled", "§cAlready disabled!.");
            lang.put("command.onlineauth.disabled", "§aOnline auth has been disabled for your account. Reconnect to the server to apply the changes.");
            lang.put("player.invalid_username", "§cInvalid username!");
            lang.put("player.connect.authenticate", "§6Пожалуйста, войдите! Используйте §f/login §6или §f/register§6.");
            lang.put("command.general.authenticated", "§aВы успешно вошли!");
            lang.put("command.general.notmatch", "§cПароли не совпадают!");
            lang.put("config.incorrect", "§cSimplerAuth config is set up wrongly!\nSee the wiki for more information on setting up the config.");
        } else {
            lang.put("command.login.notregistered", "§cYou are not registered! Use §f/register §cinstead.");
            lang.put("command.login.wrongpassword", "§cWrong password!");
            lang.put("command.login.alreadylogged", "§cAlready authenticated!");
            lang.put("command.register.alreadyregistered", "§cYou are already registered! Use §f/login §cinstead.");
            lang.put("command.register.globaltype", "§cGlobal password is set on this server, registration is not possible! Use §f/login");
            lang.put("command.changepassword.notregistered", "§cYou are not registered!");
            lang.put("command.changepassword.success", "§aChanged password successfully!");
            lang.put("command.onlineauth.globaltype", "§cGlobal password is set on this server, online auth is not possible! Use §f/login");
            lang.put("command.onlineauth.notregistered", "§cYou are not registered! Use §f/register §cbefore enabling online auth.");
            lang.put("command.onlineauth.featuredisabled", "§cOptional online auth is not enabled in this server!");
            lang.put("command.onlineauth.warning", "§cYou will lose all your data linked to your UUID, like your inventory & ender chest, statistics, advancements, villager discounts, etc.");
            lang.put("command.onlineauth.cannotenable", "§cAn official account with this username does not exist! You can't enable this feature");
            lang.put("command.onlineauth.confirmenable", "§6To confirm, use §f/onlineauth enable <password>§6. You can disable it with §f/onlineauth disable§6.");
            lang.put("command.onlineauth.alreadyenabled", "§cAlready enabled!.");
            lang.put("command.onlineauth.enabled", "§aOnline auth has been enabled for your account. Reconnect to the server to apply the changes.");
            lang.put("command.onlineauth.confirmdisable", "§6To confirm, use §f/onlineauth disable <password>§6. You can enable it again with §f/onlineauth§6.");
            lang.put("command.onlineauth.alreadydisabled", "§cAlready disabled!.");
            lang.put("command.onlineauth.disabled", "§aOnline auth has been disabled for your account. Reconnect to the server to apply the changes.");
            lang.put("player.invalid_username", "§cInvalid username!");
            lang.put("player.connect.authenticate", "§6Please, authenticate! Use §f/login §6or §f/register§6.");
            lang.put("command.general.authenticated", "§aAuthenticated successfully!");
            lang.put("command.general.notmatch", "§cPasswords do not match!");
            lang.put("config.incorrect", "§cSimplerAuth config is set up wrongly!\nSee the wiki for more information on setting up the config.");
        }
    }

}
