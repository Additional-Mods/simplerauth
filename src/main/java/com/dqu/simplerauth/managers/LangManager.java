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
        switch(language.toLowerCase()) {
            case "ru":
                lang.put("command.login.notregistered", "§cВы не зарегестрированы! Используйте §f/register");
                lang.put("command.login.wrongpassword", "§cНеверный пароль!");
                lang.put("command.login.alreadylogged", "§cВы уже вошли!");
                lang.put("command.register.alreadyregistered", "§cВы уже зарегестрированы! Используйте §f/login");
                lang.put("command.register.globaltype", "§cНа сервере установлен глобальный пароль, регистрация невозможна! Используйте §f/login");
                lang.put("command.register.passweak", "§cЭтот пароль не надежный! Пожалуйста, попробуйте более надёжный пароль.");
                lang.put("command.changepassword.notregistered", "§cВы не зарегестрированы!");
                lang.put("command.changepassword.success", "§aВы успешно изменили пароль!");
                lang.put("command.onlineauth.globaltype", "§cНа этом сервере установлен глобальный пароль, онлайн авторизация невозможна! Используйте §f/login");
                lang.put("command.onlineauth.notregistered", "§cВы не зарегестрированы! Используйте §f/register §cперед включением онлайн авторизации.");
                lang.put("command.onlineauth.featuredisabled", "§cОпциональная онлайн авторизация не включена на этом сервере!");
                lang.put("command.onlineauth.warning", "§cВы потеряете все данные привязанные к UUID, например ваш инвентарь & содержимое эндер сундука, статистика, достижения, и т.д.");
                lang.put("command.onlineauth.cannotenable", "§cОффициальный аккаунт с этим именем пользователя не существует! Вы не можете включить эту функцию");
                lang.put("command.onlineauth.confirmenable", "§6Для подтверждения, используйте §f/onlineauth enable <пароль>§6. Вы можете отключить это с помощью §f/onlineauth disable§6.");
                lang.put("command.onlineauth.alreadyenabled", "§cУже включено!");
                lang.put("command.onlineauth.enabled", "§aОнлайн авторизация была включена для вашего аккаунта. Перезайдите чтобы применить изменения.");
                lang.put("command.onlineauth.confirmdisable", "§6Для подтверждения, используйте §f/onlineauth disable <пароль>§6. Вы можете включить это снова с помощью §f/onlineauth§6.");
                lang.put("command.onlineauth.alreadydisabled", "§cУже выключено!.");
                lang.put("command.onlineauth.disabled", "§aОнлайн авторизация была выключена для вашего аккаунта. Перезайдите чтобы применить изменения.");
                lang.put("player.invalid_username", "§cНеверное имя пользователя!");
                lang.put("player.connect.authenticate", "§6Пожалуйста, войдите! Используйте §f/login §6или §f/register§6.");
                lang.put("player.connect.alreadylogged", "§cЭтот игрок уже присутствует на сервере!");
                lang.put("command.general.authenticated", "§aВы успешно вошли!");
                lang.put("command.general.notmatch", "§cПароли не совпадают!");
                lang.put("config.incorrect", "§cSimplerAuth config is set up wrongly!\nSee the wiki for more information on setting up the config.");
                break;
            case "es":
                lang.put("command.login.notregistered", "§c¡No estás registrado! Usa §f/register §cen su lugar.");
                lang.put("command.login.wrongpassword", "§c¡Contraseña incorrecta!");
                lang.put("command.login.alreadylogged", "§c¡Ya estás autentificado!");
                lang.put("command.register.alreadyregistered", "§c¡Ya estás registrado! Usa §f/login §cen su lugar.");
                lang.put("command.register.globaltype", "§c¡El servidor tiene una contraseña global, registrarse no es posible! Usa §f/login");
                lang.put("command.register.passweak", "§c¡Esta contraseña es demasiado débil! Por favor, prueba con una más segura.");
                lang.put("command.changepassword.notregistered", "§c¡No estás registrado!");
                lang.put("command.changepassword.success", "§a¡Contraseña cambiada exitosamente!");
                lang.put("command.onlineauth.globaltype", "§c¡La contraseña global está puesta en este servidor, la autentificación en línea no es posible! Use §f/login");
                lang.put("command.onlineauth.notregistered", "§c¡No estás registrado! Usa §f/register §cantes de activar la autentificación en línea.");
                lang.put("command.onlineauth.featuredisabled", "§c¡La autentificación en línea opcional no está activada en este servidor!");
                lang.put("command.onlineauth.warning", "§cPerderás todos tus datos enlazados con tu UUID, como tu inventario y cofre de ender, estadisticas, logros, descuentos de aldeanos, etc.");
                lang.put("command.onlineauth.cannotenable", "§c¡Una cuenta oficial con este nombre no existe! No puedes activar esta característica");
                lang.put("command.onlineauth.confirmenable", "§6Para confirmar, usa §f/onlineauth enable <password>§6. Puedes desactivarla con §f/onlineauth disable§6.");
                lang.put("command.onlineauth.alreadyenabled", "§c¡Ya está activada!.");
                lang.put("command.onlineauth.enabled", "§aLa autentificación en línea ha sido activada para tu cuenta. Reconéctate al servidor para aplicar los cambios.");
                lang.put("command.onlineauth.confirmdisable", "§6Para confirmar, usa §f/onlineauth disable <password>§6. Puedes volver a activarla con §f/onlineauth§6.");
                lang.put("command.onlineauth.alreadydisabled", "§c!Ya está desactivada!.");
                lang.put("command.onlineauth.disabled", "§aLa autentificación en línea ha sido desactivada para tu cuenta. Reconéctate al servidor para aplicar los cambios.");
                lang.put("player.invalid_username", "§c¡Nombre de usuario inválido!");
                lang.put("player.connect.authenticate", "§6¡Por favor, autentifícate! Usa §f/login §6o §f/register§6.");
                lang.put("player.connect.alreadylogged", "§c¡Este jugador ya está presente en el servidor!");
                lang.put("command.general.authenticated", "§a¡Autentificado exitosamente!");
                lang.put("command.general.notmatch", "§c¡Las contraseñas no coinciden!");
                lang.put("config.incorrect", "§c¡La configuración de SimplerAuth está mal configurada!\nMira la wiki para más información sobre como configurar la configuración.");
                break;
            case "cz":
                lang.put("command.login.notregistered", "§cNejsi registrován! Použij §f/register§c.");
                lang.put("command.login.wrongpassword", "§cŠpatné heslo!");
                lang.put("command.login.alreadylogged", "§cÚčet ověřen!");
                lang.put("command.register.alreadyregistered", "§cJiž jsi zaregistrován! Použij §f/login§c.");
                lang.put("command.register.globaltype", "§cNa tomto serveru je nastaveno globální heslo, registrace není možná! Použij §f/login§c.");
                lang.put("command.register.passweak", "§cTohle heslo je příliš slabé! Prosím, zkus použít silnější.");
                lang.put("command.changepassword.notregistered", "§cNejsi zaregistrován!");
                lang.put("command.changepassword.success", "§aHeslo bylo úspěšně změněno!");
                lang.put("command.onlineauth.globaltype", "§cNa tomto serveru je nastaveno globální heslo, online autentizace není možná! Použij §f/login§c.");
                lang.put("command.onlineauth.notregistered", "§cNejsi zaregistrován! Použij §f/register §cpřed zapnutím online autentizace.");
                lang.put("command.onlineauth.featuredisabled", "§cNa tomto serveru není povolena volitelná funkce online autentizace!");
                lang.put("command.onlineauth.warning", "§cZtratíte všechna data spojená s vaším UUID, jako je inventář, ender truhla, statistiky, pokroky, slevy pro vesničany atd.");
                lang.put("command.onlineauth.cannotenable", "§cOficiální účet s tímto uživatelským jménem neexistuje! Tuto funkci nelze povolit.");
                lang.put("command.onlineauth.confirmenable", "§6Pro potvrzení použij §f/onlineauth enable <heslo>§6. Můžeš ji zakázat pomocí §f/onlineauth disable§6.");
                lang.put("command.onlineauth.alreadyenabled", "§cJiž povoleno!");
                lang.put("command.onlineauth.enabled", "§aOnline autentizace byla pro tvůj účet povolena. Znovu se připoj k serveru, aby se aplikovaly změny.");
                lang.put("command.onlineauth.confirmdisable", "§6Pro potvrzení použij §f/onlineauth disable <heslo>§6. Můžeš ji znovu povolit pomocí §f/onlineauth§6.");
                lang.put("command.onlineauth.alreadydisabled", "§cJiž zakázáno!");
                lang.put("command.onlineauth.disabled", "§aOnline autentizace byla pro tvůj účet zakázána. Pro provedení změn se znovu připoj k serveru.");
                lang.put("player.invalid_username", "§cNeplatné uživatelské jméno!");
                lang.put("player.connect.authenticate", "§6Prosím, ověř svůj účet! Použij §f/login §6nebo §f/register§6.");
                lang.put("player.connect.alreadylogged", "§cTento hráč je již na serveru přítomen!");
                lang.put("command.general.authenticated", "§aOvěření proběhlo úspěšně!");
                lang.put("command.general.notmatch", "§cHesla se neshodují!");
                lang.put("config.incorrect", "§cKonfigurace SimplerAuth je špatně nastavena!\nDalší informace o nastavení konfigurace naleznete na wiki.");
                break;
            default:
                lang.put("command.login.notregistered", "§cYou are not registered! Use §f/register §cinstead.");
                lang.put("command.login.wrongpassword", "§cWrong password!");
                lang.put("command.login.alreadylogged", "§cAlready authenticated!");
                lang.put("command.register.alreadyregistered", "§cYou are already registered! Use §f/login §cinstead.");
                lang.put("command.register.globaltype", "§cGlobal password is set on this server, registration is not possible! Use §f/login");
                lang.put("command.register.passweak", "§cThis password is too weak! Please, try a stronger one.");
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
                lang.put("player.connect.alreadylogged", "§cThis player is already present on the server!");
                lang.put("command.general.authenticated", "§aAuthenticated successfully!");
                lang.put("command.general.notmatch", "§cPasswords do not match!");
                lang.put("config.incorrect", "§cSimplerAuth config is set up wrongly!\nSee the wiki for more information on setting up the config.");
        }

    }

}
