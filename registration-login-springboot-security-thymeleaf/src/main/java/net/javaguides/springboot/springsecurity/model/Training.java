package net.javaguides.springboot.springsecurity.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Training {


    private String image_id;
    private int id;
    private String sharing_type;

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void readId() {
        try
        {

            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/users_database", "root", "Physics123");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select id, image_id from users_image_record where display_status=0 and user_name='hiteshsapkota@gmail.com';");

            if (rs.next() == false) {
                this.id = 0;
                this.image_id="na";

            }
            else
                {
                this.id = rs.getInt(1);
                this.image_id = rs.getString(2);
                stmt.executeUpdate("update users_image_record set display_status = 1  where id ="+Integer.toString(this.id)+";");


                }




                con.close();
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public String getSharing_type() {
        return sharing_type;
    }

    public void setSharing_type(String sharing_type) {
        this.sharing_type = sharing_type;

    }
    public void storeSharing_type()
    {

        try
        {

            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/users_database", "root", "Physics123");
            Statement stmt = con.createStatement();
            System.out.println(sharing_type);
            System.out.println(image_id);
            if (sharing_type.equals("share"))
            {
                stmt.executeUpdate("update users_image_record set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");
            }
            else if (sharing_type.equals("not_share"))
            {
                stmt.executeUpdate("update users_image_record set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
            }


            con.close();
        } catch (Exception e)
        {
            System.out.println(e);
        }

    }
}
