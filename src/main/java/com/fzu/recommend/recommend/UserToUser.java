package com.fzu.recommend.recommend;

public class UserToUser {

    private int userId1;

    private int userId2;

    public int getUserId1() {
        return userId1;
    }

    public void setUserId1(int userId1) {
        this.userId1 = userId1;
    }

    public int getUserId2() {
        return userId2;
    }

    public void setUserId2(int userId2) {
        this.userId2 = userId2;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserToUser that = (UserToUser) o;
        return (userId1 == that.userId1 && userId2 == that.userId2)
                || (userId1 == that.userId2 && userId2 == that.userId1);
    }



    @Override
    public int hashCode(){
        int min = 0, max = 0;
        max = Math.max(userId1, userId2);
        min = Math.min(userId1, userId2);
        int result = 17;
        result = 37 * result + max;
        result  = 37 * result + min;
        return result;
    }

}
