package com.boilerplate.infrastructure.common.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_sequences")
@Getter @Setter
public class DocumentSequenceEntity {

    @EmbeddedId
    private DocumentSequenceId id;

    @Column(nullable = false)
    private int lastSeq;
}
