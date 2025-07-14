package ticket.booking.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UserBookingService{

    private ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;

    private User user;

    private final String USER_FILE_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(new File(USER_FILE_PATH), new TypeReference<List<User>>() {});
    }


    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(user1 -> user1.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();
        if (foundUser.isPresent()) {
            this.user = foundUser.get();  // Assign the actual user object from file
            return true;
        }
        return false;
    }

    public Boolean signUp(User user1){
        boolean userExists = userList.stream().anyMatch(u -> u.getName().equals(user1.getName()));
        if (userExists) {
            System.out.println("User already exists.");
            return Boolean.FALSE;
        }
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            System.out.println("Failed to save user.");
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_FILE_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBookings(){
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        }
    }

    // todo: Complete this function
    public Boolean cancelBooking(String ticketId) {
        Optional<Ticket> ticketToCancel = user.getTicketsBooked().stream()
                .filter(ticket -> ticket.getTicketId().equals(ticketId))
                .findFirst();

        if (ticketToCancel.isEmpty()) {
            System.out.println("No ticket found with ID " + ticketId);
            return false;
        }

        Ticket ticket = ticketToCancel.get();
        Train train = ticket.getTrain();
        List<List<Integer>> seats = train.getSeats();

        // Reset the first booked seat (assumes only one seat per ticket)
        for (List<Integer> seat : seats) {
            for (int j = 0; j < seat.size(); j++) {
                if (seat.get(j) == 1) {
                    seat.set(j, 0);
                    break;
                }
            }
        }

        try {
            // Update train file
            new TrainService().addTrain(train);

            // Remove ticket and update user file
            user.getTicketsBooked().remove(ticket);
            userList = userList.stream()
                    .map(u -> u.getUserId().equals(user.getUserId()) ? user : u)
                    .collect(Collectors.toList());
            saveUserListToFile();

            System.out.println("Ticket with ID " + ticketId + " has been canceled.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to update files.");
            return false;
        }
    }



    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    // Mark the seat as booked
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);

                    // Update the train file (persist seat changes)
                    trainService.addTrain(train);

                    // Prompt or use actual source, destination, and date
                    Scanner sc = new Scanner(System.in);
                    System.out.print("Enter source station: ");
                    String source = sc.nextLine();

                    System.out.print("Enter destination station: ");
                    String destination = sc.nextLine();

                    System.out.print("Enter date of travel (yyyy-MM-dd): ");
                    String travelDateInput = sc.nextLine();

                    // Parse input into ISO 8601 format with "Z"
                    String formattedTravelDate = travelDateInput + "T18:30:00Z"; // you can improve time capture later

                    // Create ticket
                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(),     // ticket_id
                            user.getUserId(),                 // user_id
                            source,
                            destination,
                            formattedTravelDate,
                            train                                 // full train object
                    );

                    // Optional: generate human-readable ticket info
                    String ticketInfo = String.format(
                            "Ticket ID: %s belongs to User %s from %s to %s on %s",
                            ticket.getTicketId(),
                            ticket.getUserId(),
                            ticket.getSource(),
                            ticket.getDestination(),
                            ticket.getDateOfTravel()
                    );
                    ticket.setTicketInfo(ticketInfo);

                    // Add ticket to user's list
                    user.getTicketsBooked().add(ticket);

                    // Persist updated user list
                    saveUserListToFile();

                    return true;  // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }
}