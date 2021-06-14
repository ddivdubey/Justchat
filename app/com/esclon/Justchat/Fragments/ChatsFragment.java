package com.escalon.JustChat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.escalon.JustChat.Adapter.UserAdapter;
import com.escalon.JustChat.Model.ChatList;
import com.escalon.JustChat.Model.Users;
import com.escalon.JustChat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ChatsFragment extends Fragment{

    private UserAdapter userAdapter;
    private List<Users> mUsers;

    private List<Users> mUserstemp;

    FirebaseUser fuser;
    DatabaseReference reference;
    private RecyclerView recyclerView;

    private List<ChatList> usersList;
    private List<Users> searchuserlist;
    private List<Users> searchuserlist2;

    public ChatsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats,
                container,
                false);
        recyclerView = view.findViewById(R.id.recycler_view2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();
//        searchuserlist = new ArrayList<>();
//        searchuserlist2 = new ArrayList<>();



        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatList chatList = dataSnapshot.getValue(ChatList.class);
                    usersList.add(chatList);
                }
//                searchuserlist = new ArrayList<>(usersList);
                chatList();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;


    }

    private void chatList() {

        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users user = dataSnapshot.getValue(Users.class);
                    for(ChatList chatList : usersList){
                        if(user.getId().equals(chatList.getId())){
                            mUsers.add(user);
                        }
                    }
                }

//                for (DataSnapshot dataSnapshot:snapshot.child("Chats").getChildren()){
//                    Users users = dataSnapshot.getValue(Users.class);
//                    for (Chat chat: chats){
//                        if(chat.getMessage().equals(users.getId())){
//
//                        }
//                    }
//                }
                searchuserlist = new ArrayList<>(mUsers);

                userAdapter = new UserAdapter(getContext(),mUsers);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        android.widget.SearchView searchView = (android.widget.SearchView) menuItem.getActionView();
        menuItem.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                userAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                userAdapter.getFilter().filter(s);
                return false;
            }
        });
//        check useradapter

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


////
//    @Override
//    public Filter getFilter() {
//        return filter;
//    }
//
//    Filter filter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence charSequence) {
//            List<ChatList> filterusers = new ArrayList<>();
//            if(charSequence.toString().isEmpty()){
//                filterusers.addAll(userstempchatlist);
//            }
//            else {
//                for (ChatList chatList: userstempchatlist){
//                    if(chatList.toString().toLowerCase().contains(charSequence.toString().toLowerCase())){
//                        filterusers.add(chatList);
//                    }
//                }
//            }
//            FilterResults filterResults  = new FilterResults();
//            filterResults.values = usersList;
//            return filterResults;
//        }
//
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
////        updating ui
//            usersList.clear();
//            usersList.addAll((Collection<? extends ChatList>) filterResults.values);
//            userAdapter.notifyDataSetChanged();
//        }
//    };
}
