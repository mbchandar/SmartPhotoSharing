package com.hmi.smartphotosharing.util;

import java.util.Comparator;

import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.News;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.json.User;

public class Sorter
{
    public static Comparator<News> NEWS_SORTER_TIME = new Comparator<News>() {
        public int compare(News o1, News o2) {
            return (int)(o2.time - o1.time);
        }
    };
    
    public static Comparator<Group> GROUP_SORTER_UPDATES = new Comparator<Group>() {
        public int compare(Group o1, Group o2) {
            return o2.totalnew - o1.totalnew;
        }
    };

    public static Comparator<Group> GROUP_SORTER_ABC = new Comparator<Group>() {
        public int compare(Group o1, Group o2) {
            return o1.name.compareToIgnoreCase(o2.name);
        }
    };
    
    public static Comparator<Group> GROUP_SORTER_LOC = new Comparator<Group>() {
        public int compare(Group o1, Group o2) {
            return Integer.parseInt(o2.locationlink) - Integer.parseInt(o1.locationlink);
        }
    };
    
    public static Comparator<Subscription> SUBSCRIPTIONS_SORTER = new Comparator<Subscription>() {
        public int compare(Subscription o1, Subscription o2) {
            return o2.totalnew - o1.totalnew;
        }
    };   
    
    public static Comparator<User> USER_SORTER = new Comparator<User>() {
        public int compare(User o1, User o2) {
            return o1.rname.compareToIgnoreCase(o2.rname);
        }
    };  
}

