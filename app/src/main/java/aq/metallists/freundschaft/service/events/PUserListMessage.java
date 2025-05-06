package aq.metallists.freundschaft.service.events;

import java.util.ArrayList;

import aq.metallists.freundschaft.FSRoomMember;

public class PUserListMessage {
    public ArrayList<FSRoomMember> items;

    public PUserListMessage(ArrayList<FSRoomMember> _itms) {
        items = _itms;
    }
}
