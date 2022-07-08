package edu.hm.cs.katz.swt2.agenda.persistence;

import java.util.Collection;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;

/**
 * Modellklasse für die Speicherung der Aufgaben. Enthält die Abbildung auf eine
 * Datenbanktabelle in Form von JPA-Annotation.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@Entity
public class Task {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Length(min = 8, max = 32)
    @Column(length = 32)
    private String title;

    @NotNull
    @Length(min = 10, max = 100)
    @Column(length = 100)
    private String shortDescription;

    @NotNull
    @Length(min = 10, max = 150)
    @Column(length = 150)
    private String longDescription;

    @NotNull
    @ManyToOne
    private Topic topic;

    @NotNull
    private String deadline;

    @ManyToOne
    @NotNull
    private User creator;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Collection<Status> status;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskTypeEnum taskType = TaskTypeEnum.NOTYPE;
    
    /**
     * JPA-kompatibler Kostruktor. Wird nur von JPA verwendet und darf private sein.
     */
    public Task() {
        // JPA benötigt einen Default-Konstruktor!
    }

    /**
     * Konstruktor zum Erstellen eines neuen Tasks.
     * 
     * @param topic Topic, darf nicht null sein.
     * @param title Titel, darf nicht null sein.
     */
    public Task(final Topic topic, final String title, final String shortDescription, final String longDescription,
            final User createdBy, String deadline, TaskTypeEnum taskType) {
        this.topic = topic;
        topic.addTask(this);
        this.title = title;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.creator = createdBy;
        this.deadline = deadline;
        this.taskType = taskType;
    }

    @Override
    public String toString() {
        return "Task \"" + title + "\"";
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
    
    public TaskTypeEnum getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }

    /*
     * Standard-Methoden. Es ist sinnvoll, hier auf die Auswertung der Assoziationen
     * zu verzichten, nur die Primärschlüssel zu vergleichen und insbesonderen
     * Getter zu verwenden, um auch mit den generierten Hibernate-Proxys kompatibel
     * zu bleiben.
     */

    @Override
    public int hashCode() {
        return Objects.hash(id, topic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;
        return Objects.equals(getId(), other.getId());
    }

}
