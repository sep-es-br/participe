package br.gov.es.participe.model;

import br.gov.es.participe.controller.dto.HighlightParamDto;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Date;

@NodeEntity
public class Highlight extends Attend {

  public Highlight() {
  }

  public Highlight(HighlightParamDto highlightParamDto) {
    this.setId(highlightParamDto.getId());
    this.setFrom(highlightParamDto.getFrom());
    setTime(new Date());

    if(highlightParamDto.getLocality() != null) {
      this.setLocality(new Locality(highlightParamDto.getLocality()));
    }

    if(highlightParamDto.getMeeting() != null) {
      this.setMeeting(new Meeting(highlightParamDto.getMeeting()));
    }

    if(highlightParamDto.getPersonMadeBy() != null) {
      this.setPersonMadeBy(new Person(highlightParamDto.getPersonMadeBy()));
    }

    if(highlightParamDto.getPlanItem() != null) {
      this.setPlanItem(new PlanItem(highlightParamDto.getPlanItem()));
    }

    if(highlightParamDto.getConference() != null) {
      Conference conference = new Conference();
      conference.setId(highlightParamDto.getConference());
      this.setConference(conference);
    }
  }
}
