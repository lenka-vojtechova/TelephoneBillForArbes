package com.phonecompany.billing;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CallsInList implements TelephoneBillCalculator {
    private int id;
    private long number;
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal price;
    private int minutesVeryLow;
    private int minutesLow;
    private int minutesHigh;
    private CallsInList next;
    private static CallsInList first;
    private static CallCount callCount;
    private static BigDecimal totalBillPrice;
    private static final BigDecimal priceHigh = new BigDecimal("1");
    private static final BigDecimal priceVeryLow = new BigDecimal("0.2");
    private static final BigDecimal priceLow = new BigDecimal("0.5");
    private static final LocalTime highTaxStarts = LocalTime.parse("08:00:00");
    private static final LocalTime lowTaxStarts = LocalTime.parse("16:00:00");
    public CallsInList(){
        totalBillPrice = new BigDecimal(0);
        price = new BigDecimal(0);
        id = 0;
    }

    public BigDecimal calculate (String phoneLog){
        try{
            System.out.println("------------------------------------------------------");
            System.out.println("Listing calls from phone log...");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            char separator = ',';
            char separatorRow = '\r';

            int i = 0;
            int phoneLogEnd = phoneLog.length();
            while (i < phoneLogEnd) {

                int numberEnd = phoneLog.indexOf(separator,i)-1;
                int startTimeStart = numberEnd+2;
                int startTimeEnd = phoneLog.indexOf(separator,startTimeStart);
                int endTimeStart = startTimeEnd+1;
                int endTimeEnd = phoneLog.indexOf(separatorRow,i);

                String number = phoneLog.substring(i,numberEnd);
                String startTime = phoneLog.substring(startTimeStart,startTimeEnd);
                String endTime = phoneLog.substring(endTimeStart,endTimeEnd);

                CallsInList.add(Long.parseLong(number),LocalDateTime.parse(startTime, formatter),LocalDateTime.parse(endTime, formatter));
                System.out.println("Call to number " + number + " start " + startTime + " end "+endTime);

                i=endTimeEnd+2;
            }

            CallsInList.runPromo();
            CallsInList.calculateTotalBillPrice();

            System.out.println("Telephone Billing finished calculation successfully.");
            System.out.println("------------------------------------------------------");
        }
        catch (Exception e){
            System.out.println("Error during calculating price.");
            throw e;
        }
        return totalBillPrice;
    }

    private static void add(long number, LocalDateTime start, LocalDateTime end) {
        if (first==null){
            first = new CallsInList();
            first.number=number;
            first.start=start;
            first.end=end;
            first.calculateMinutesAndPrice();
            first.addCallCount();
        } else {
            int id=1;
            CallsInList tmp = first;
            while (tmp.next!=null){
                id++;
                tmp = tmp.next;
            }
            tmp.next = new CallsInList();
            tmp.next.id=id;
            tmp.next.number = number;
            tmp.next.start = start;
            tmp.next.end = end;
            tmp.next.calculateMinutesAndPrice();
            tmp.next.addCallCount();
        }
    }

    private void calculateMinutesAndPrice() {
        if (start==null||end==null) {
            System.out.println("Attention! Start or end time of call id: " + id + " to number: " + this.number + " is not set.");
        } else if (Duration.between(start,end).toSeconds()<0){
            System.out.println("Attention! Call id: "+ id + " to number: " + this.number + " finished before has started... start: " + start + " end: " + end);
        } else if (Duration.between(start, end).toSeconds()>0){
            LocalDateTime temp = start;
            while (Duration.between(temp,end).toSeconds()>0){
                // call with duration less than second is spared, is for zero minutes and zero price
                if (minutesLow+minutesHigh+minutesVeryLow>=5){
                    //current tax very low
                    minutesVeryLow++;
                    price = price.add(priceVeryLow);
                } else if(Duration.between(temp.toLocalTime(),highTaxStarts).toSeconds()>0){
                    //current tax low
                    minutesLow++;
                    price = price.add(priceLow);
                } else if (Duration.between(temp.toLocalTime(),lowTaxStarts).toSeconds()>=0) {
                    //current tax high
                    minutesHigh++;
                    price = price.add(priceHigh);
                } else {
                    //current tax low
                    minutesLow++;
                    price = price.add(priceLow);
                }
                long sec = 60-temp.getSecond();
                temp = temp.plusSeconds(sec);
            }
        }
    }

    private void addCallCount(){
        if (callCount==null){
            //if its first call create callCount
            callCount = new CallCount();
            callCount.number = number;
            callCount.count = 1;
        } else {
            //search number in call count
            boolean found = false;
            CallCount temp = callCount;
            while (temp.next != null) {
                if (temp.number == number) {
                    //if found increment count
                    found = true;
                    temp.count++;
                }
                temp = temp.next;
            }
            if (temp.number == number){
                found = true;
                temp.count++;
            }
            if (!found) {
                //if not found add to end
                temp.next = new CallCount();
                temp.next.number = number;
                temp.next.count = 1;
            }
        }
    }

    private static void runPromo(){
        System.out.println("Running promo...");
        CallCount aspiring = new CallCount();
        aspiring.count = 0;
        aspiring.number = 0;
        CallCount temp = callCount;
        while (temp!=null){
            if (temp.count>aspiring.count){
                //if count larger make aspiring
                aspiring = temp;
            } else if (temp.count==aspiring.count&&temp.number>aspiring.number){
                //if same but has large arithmetic value make aspiring
                aspiring = temp;
            }
            temp = temp.next;
        }
        if (aspiring.count>0){
            //aspiring number wins promo
            CallsInList.applyPromoPrice(aspiring);
        }

    }

    private static void applyPromoPrice(CallCount callCount) {
        //calls for callCount number are for promo price
        BigDecimal promoPrice = new BigDecimal(0);
        CallsInList temp = first;
        while (temp!=null){
            if (temp.number==callCount.number){
                temp.price = promoPrice;
            }
            temp = temp.next;
        }
    }

    private static void calculateTotalBillPrice(){
        CallsInList temp = first;
        while (temp!=null){
            //add call price to total bill
            totalBillPrice = totalBillPrice.add(temp.price);
            temp = temp.next;
        }
    }


}
