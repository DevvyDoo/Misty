package sh.niall.misty;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;
import sh.niall.misty.cogs.*;
import sh.niall.misty.utils.audio.AudioGuildManager;
import sh.niall.misty.utils.misty.Config;
import sh.niall.misty.utils.misty.ConfigLoader;
import sh.niall.misty.utils.misty.Database;
import sh.niall.misty.utils.playlists.SongCache;
import sh.niall.yui.Yui;
import sh.niall.yui.cogs.commands.prefix.PrefixManager;
import sh.niall.yui.exceptions.CommandException;
import sh.niall.yui.exceptions.PrefixException;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;

public class Misty {

    public static Config config;
    public static Database database;
    public static Yui yui;
    public static Long ownerId = null;

    public static void main(String[] args) throws LoginException, FileNotFoundException, PrefixException, CommandException {
        // Initialize globals
        config = ConfigLoader.loadConfig();
        database = new Database();

        // Generate JDA Builder
        JDABuilder builder = JDABuilder.createDefault(config.getDiscordToken());
        builder.setAudioSendFactory(new NativeAudioSendFactory());
        builder.setActivity(Activity.watching("for messages - ?help"));

        // Setup Yui
        PrefixManager prefixManager = new PrefixManager(config.getDiscordPrefixes(), config.getBackupPrefix());
        yui = new Yui(builder, prefixManager, true);

        // Create the audio manager
        AudioGuildManager audioGuildManager = new AudioGuildManager(yui);
        SongCache songCache = new SongCache(yui, audioGuildManager.getAudioPlayerManager());

        // Add the cogs in
        yui.addCog(
                new Admin(),
                new ErrorHandler(),
                new Internet(),
                new Music(audioGuildManager),
                new Playlists(audioGuildManager, songCache),
                new Reminders(),
                new Social(),
                new Tags(),
                new Utilities()
        );

        // Build JDA
        builder.build();
        LoggerFactory.getLogger(Misty.class).info("I'm online and ready to go!");
    }

}
