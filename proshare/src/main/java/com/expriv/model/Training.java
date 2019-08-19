package com.expriv.model;

import com.expriv.service.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;


@Repository
public class Training{

    private String image_id;
    private String image_path;
    private String description;

    private int id;
    private String options;
    private String submittype;
    private JdbcTemplate jdbcTemplate;
    private String username;
    private int trainingInstances;
    private Index index;
    private boolean update;
    private boolean invalidInput;
    private String donotshare;
    private String share;
    private String skip;
    private int sharingDecision;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    public String getUsername() {
        return username;
    }

    public Index getIndex() {
        Index index = new Index();
        ConfigurationService configurationService = new ConfigurationService();
        configurationService.setParams();
        index.setTrainBatchSize(configurationService.getTrain_batch_size());
        index.setUsername(this.username);
        index.setJdbcTemplate(jdbcTemplate);
        index.setProgress();
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public void setUsername(Object principal) {

        if (principal instanceof UserDetails) {
            this.username = ((UserDetails)principal).getUsername();
        }
        else {
            this.username = principal.toString();
        }
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }


    public int getTrainingInstances() {
        return trainingInstances;
    }

    public boolean isUpdate() {
        return update;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isInvalidInput() {
        return invalidInput;
    }

    public void setInvalidInput(boolean invalidInput) {
        this.invalidInput = invalidInput;
    }


    public String getDonotshare() {
        return donotshare;
    }

    public void setDonotshare(String donotshare) {
        this.donotshare = donotshare;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getSkip() {
        return skip;
    }

    public void setSkip(String skip) {
        this.skip = skip;
    }


    public int getSharingDecision() {
        return sharingDecision;
    }

    public void setSharingDecision(int sharingDecision) {
        this.sharingDecision = sharingDecision;
    }

    public void setTrainingInstances() {

        String username=this.username;
        String sql = "select * from training where display_status=1 and user_name=?";
        List<Record> records=jdbcTemplate.query(sql, new Object[] { username },new RecordRowMapper());
        this.trainingInstances=records.size();

    }

    public void readId() {
        try
        {

            String username=this.username;
            ConfigurationService configurationService=new ConfigurationService();
            configurationService.setParams();
            String sql = "select * from training where display_status=0 and user_name=?";
            List<Record> records=jdbcTemplate.query(sql, new Object[] { username },new RecordRowMapper());

            if (records.isEmpty())
            {
                this.id=0;
                this.image_path="na";
                this.image_id="na";
            }
            else {

                Record record=records.get(0);
                this.id = record.getId();
                this.image_path = record.getImage_path();
                this.description=record.getDescription();
                this.image_id=record.getImage_id();



            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }


    public void getPrevious()
    {
        try
        {
            boolean success = false;
            String username=this.username;
            ConfigurationService configurationService=new ConfigurationService();
            configurationService.setParams();
            String sql = "select * from training where id=?";
            int i=1;
            while (true) {

                List<Record> records = jdbcTemplate.query(sql, new Object[]{(this.id - i)}, new RecordRowMapper());
                if (!records.isEmpty()) {

                    Record record = records.get(0);
                    if (record.getUser_name().equals(this.username)) {
                        sql = "select * from training where id=? and sharing_decision is not null";
                        List<Record> r1 = jdbcTemplate.query(sql, new Object[]{(this.id - 1)}, new RecordRowMapper());
                        sql = "select * from training where id=? and sharing_decision is null";
                        List<Record> r2 = jdbcTemplate.query(sql, new Object[]{(this.id - 1)}, new RecordRowMapper());
                        success = true;
                        this.id = record.getId();
                        this.image_path = record.getImage_path();
                        this.image_id = record.getImage_id();
                        this.description = record.getDescription();

                        if (r1.isEmpty()) {
                            this.skip = "active focus";
                            this.share = "";
                            this.donotshare = "";
                        }
                        else {


                            if (record.getSharing_decision() == 1) {
                                this.skip="";
                                this.share = "active focus";
                                this.donotshare = "";
                            }
                            else if (record.getSharing_decision() == 0) {
                                this.skip="";
                                this.share = "";
                                this.donotshare = "active focus";
                            }


                        }

                    }

                }

                if (success==true || (this.id-i)==0)
                {
                    break;
                }
                i=i+1;

            }

        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }

    public void getNext()
    {
        try
        {
        String username=this.username;
        ConfigurationService configurationService=new ConfigurationService();
        configurationService.setParams();
        String sql = "select * from training where id=?";
        List<Record> records=jdbcTemplate.query(sql, new Object[] { (this.id+1) },new RecordRowMapper());
        this.id =0;
        this.image_path="na";
        if (!records.isEmpty()) {
            Record record=records.get(0);
            if (record.getUser_name().equals(this.username))
            {

                this.id = record.getId();
                this.image_path = record.getImage_path();
                this.description=record.getDescription();
            }

        }

    }
        catch (Exception e)
    {
        System.out.println(e);
    }
    }


    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getSubmittype() {
        return submittype;
    }

    public void setSubmittype(String submittype) {
        this.submittype = submittype;
    }

    public void updateDisplayStatus()
    {
        System.out.println("Updating Display Status");
        System.out.println(this.id);
        String update_query="update training set display_status = 1  where id ="+Integer.toString(this.id);
        jdbcTemplate.update(update_query);
    }

    public void storeSharing_type()
    {
        System.out.println("Storing Sharing type");
        System.out.println(jdbcTemplate);
        try
        {


            if (options.equals("share"))
            {

                jdbcTemplate.update("update training set sharing_decision = 1  where id ="+Integer.toString(this.id)+";");
            }
            else if (options.equals("donotshare"))
            {

                jdbcTemplate.update("update training set sharing_decision = 0  where id ="+Integer.toString(this.id)+";");
            }

            String update_query="update training set display_status = 1  where id ="+Integer.toString(this.id);
            jdbcTemplate.update(update_query);



        } catch (Exception e)
        {
            System.out.println(e);
        }

    }

    public void addUserPayment()

    {
        System.out.println("Before getting query");
        String sql = "select * from payment where user_name=?";
        List<Payment> payments=jdbcTemplate.query(sql, new Object[] { username },new PaymentRowMapper());
        System.out.println("After getting query");
        if (payments.isEmpty())
        {
            Object[] params = new Object[] {this.username, 1, "NA"};
            int[] types = new int[] { Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
            String query = "INSERT INTO payment (user_name, generate, code) VALUES (?, ?, ?)";
            jdbcTemplate.update(query, params, types);

        }



    }

}
