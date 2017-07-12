package com.chelsenok.wifidirect;

enum Status {

    Client {
        @Override
        int getIntent() {
            return 0;
        }
    },

    GroupOwner {
        @Override
        int getIntent() {
            return 15;
        }
    };

    abstract int getIntent();

}
