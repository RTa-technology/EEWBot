package net.teamfruit.eewbot.command.impl;

import java.net.URL;
import java.util.Optional;

import javax.xml.bind.JAXB;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.teamfruit.eewbot.EEWBot;
import net.teamfruit.eewbot.command.ICommand;
import net.teamfruit.eewbot.entity.DetailQuakeInfo;
import net.teamfruit.eewbot.entity.QuakeInfo;
import net.teamfruit.eewbot.gateway.QuakeInfoGateway;
import reactor.core.publisher.Mono;

public class QuakeInfoCommand implements ICommand {

	@Override
	public Mono<Void> execute(final EEWBot bot, final MessageCreateEvent event, final String lang) {
		return Mono.zip(event.getMessage().getChannel(),
				Mono.fromCallable(() -> {
					Optional<String> url = event.getMessage().getContent()
							.map(str -> str.split(" "))
							.filter(array -> array.length>=3)
							.map(array -> array[2]);
					if (!url.isPresent()) {
						final QuakeInfo info = JAXB.unmarshal(new URL(QuakeInfoGateway.REMOTE_ROOT+QuakeInfoGateway.REMOTE), QuakeInfo.class);
						url = info.getRecords().stream().findFirst()
								.flatMap(record -> record.getItems().stream().findFirst())
								.map(item -> item.getUrl());
					}

					final DetailQuakeInfo detail = JAXB.unmarshal(new URL(url.get()), DetailQuakeInfo.class);
					return detail;
				}))
				.flatMap(tuple -> tuple.getT1().createMessage(tuple.getT2().createMessage(lang)))
				.then();
	}

}
