package com.boolean_coercion.dscrdhmdbridge;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class Main {
	public static void main(String[] args) throws Exception {
		JDA api = new JDABuilder(AccountType.BOT) // initializing
				.setToken(Helpers.TOKEN)          // the JDA
				.addEventListener(new Listener()) // instance
				.buildBlocking();
		api.getTextChannelsByName("botlog", false).get(0)
				.sendMessage("Good morning!").queue();

		double last = System.currentTimeMillis()/1000.0; // this is to make sure i don't overload servers
		String lastId = ""; // making sure not the send the same message twice

		while(true){
			try {
				JSONObject chatsJson = new JSONObject()                            // body
						.put("chat_token", Helpers.HMD_TOKEN)                      // of the
						.put("usernames", new JSONArray().put("boolean_coercion")) // request
						.put("after", last);

				JSONArray messages = Helpers.newCall("chats", chatsJson) // send the request
						.getJSONObject("chats")                                   // and store the resulting
						.getJSONArray("boolean_coercion");                   // array of messages

				// loop through all messages and send the new ones
				for(int i = 0; i < messages.length(); i++) {
					JSONObject message = messages.getJSONObject(i); // actual message object

					String id = message.getString("id"); // this is to make
					if(id.equals(lastId)) continue;           // sure i don't send
					lastId = id;                              // the same message twice

					last = message.getDouble("t"); // not to overload the servers

					Calendar time = Calendar.getInstance();     // this is for
					time.setTimeInMillis((long) (last * 1000)); // the time formatting

					String safe = message.getString("msg")                         // safe string
							.replaceAll("`[A-Za-z0-9]([^`]*)`", "$1") // for sending
							.replace('`', '\'');

					if(message.has("channel")) { // if this is a channel message
						String channel = message.getString("channel"); // in-game channel
						List<TextChannel> temp = api.getTextChannelsByName(channel, true); // list of possible discord channels

						if(temp.isEmpty()) continue; // if the channel doesn't exist in discord, skip the message

						TextChannel ch = temp.get(0); // get the first result otherwise and send there
						ch.sendMessageFormat("```\n%02d%02d %s %s :::%s:::\n```",            // actually send
								time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),   // the discord
								channel, message.getString("from_user"), safe).queue(); // message


					} else { // else, this is a tell message
						String from = message.getString("from_user"); // user who sent the message
						String to = message.getString("to_user"); // user who received the message

						String str = to.equals("boolean_coercion") ? "from" : "to"; // was the message sent to me or by me?
						String user = to.equals("boolean_coercion") ? from : to; // name of user, to use later

						TextChannel tells = api.getTextChannelById("367781220756619266"); // chats.tell dedicated channel

						tells.sendMessageFormat("```\n%02d%02d %s %s :::%s:::\n```",
								time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
								str, user, safe).queue();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				TextChannel botlog = api.getTextChannelsByName("botlog", true).get(0);
				botlog.sendMessageFormat("<@214732126950522880>, Encountered an exception: ```\n%s\n```", e.getMessage()).queue();
			} finally {
				Thread.sleep(2000); // sleep after going through messages or after exception
			}
		}
	}
}

class Listener extends ListenerAdapter {
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event){
		TextChannel channel = event.getChannel();
		String chName = channel.getName();
		Message message = event.getMessage();
		String rawContent = event.getMessage().getRawContent();
		String safeRaw = rawContent.replaceAll("`[A-Za-z0-9]([^`]*)`", "$1").replace('`', '\'');

		TextChannel botlogs = event.getJDA().getTextChannelById("367650691252224001");

		if(event.getAuthor().isBot()) return;

		if(chName.equals("commands")){
			if(rawContent.startsWith("!token")){
				Helpers.HMD_TOKEN = rawContent.substring("!token".length()+1);
				message.addReaction("\uD83D\uDC4D").queue();
				return;
			}
		}

		JSONObject msgJson;
		if(chName.equals("chats_dot_tell")){
			if(!rawContent.contains(" ")) return;

			msgJson = new JSONObject()
					.put("chat_token", Helpers.HMD_TOKEN)
					.put("username", "boolean_coercion")
					.put("tell", rawContent.split(" ")[0])
					.put("msg", rawContent.substring(rawContent.indexOf(" ")+1));
		} else {
			msgJson = new JSONObject()
					.put("chat_token", Helpers.HMD_TOKEN)
					.put("username", "boolean_coercion")
					.put("channel", chName)
					.put("msg", rawContent);
		}

		Helpers.newCall("create_chat", msgJson, new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				boolean isTell = !msgJson.has("channel");
				message.addReaction("\u274C").queue();

				botlogs.sendMessageFormat("Failed to send ```\n%s\n```to **%s**: timed out.",
						isTell?safeRaw.substring(safeRaw.indexOf(" ")+1):safeRaw,
						isTell?msgJson.getString("tell"):chName).queue();

				System.err.println("Error: " + e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				boolean isTell = !msgJson.has("channel");
				message.delete().reason("Sent the message to in-game chat.").queue();

				botlogs.sendMessageFormat("Sent ```\n%s\n```to **%s**.\nReturn: `%s`.",
						isTell?safeRaw.substring(safeRaw.indexOf(" ")+1):safeRaw,
						isTell?msgJson.getString("tell"):chName,
						response.body().string()).queue();
			}
		});
	}
}
