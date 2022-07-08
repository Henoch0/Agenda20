package edu.hm.cs.katz.swt2.agenda.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class TaskRepositoryTest {
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    TopicRepository topicRepository;
    
    private final static String UUID_BASE = "12345678901234567890123456789012345";
    
    @Test
    public void topicRepositoryDeliversTopicsOrdered() {
        User user = new User("tiffy", "Tiffy", "skrrrrr", false);
        userRepository.save(user);
        
        Topic a = new Topic(UUID_BASE + "1", "Ttttttttttttttttttt", "Aaaaaaaaaaa", "Aaaaaaaaaaa aaaaaaaaaa aaaaaaaaaa", user);
        topicRepository.save(a);
        
        Topic b = new Topic(UUID_BASE + "2", "Xxxxxxxxxxxxxxxxxxx", "Bbbbbbbbbbb", "Bbbbbbbbbbb bbbbbbbbbb bbbbbbbbbb", user);
        topicRepository.save(b);
        
        Topic c = new Topic(UUID_BASE + "3", "Uuuuuuuuuuuuuuuuuuu", "Ccccccccccc", "Ccccccccccc cccccccccc cccccccccc", user);
        topicRepository.save(c);
        
        List<Topic> topics = topicRepository.findByCreatorOrderByTitleAsc(user);
        
        assertEquals(3, topics.size());
        assertEquals(a, topics.get(0));
        assertEquals(c, topics.get(1));
        assertEquals(b, topics.get(2));
        
    }
    
}
