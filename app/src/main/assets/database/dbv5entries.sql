INSERT INTO User (first_name, last_name, username, email, password, bio, image)
VALUES
    ('John', 'Doe', 'johndoe', 'johndoe@example.com', 'password123', 'Hello, my name is John.', (SELECT readfile('./images/lion.jpg'))),
    ('Jane', 'Smith', 'janesmith', 'janesmith@example.com', 'password456', 'Hello, my name is Jane.', (SELECT readfile('./images/woman.jpg'))),
    ('Alice', 'Johnson', 'alicejohnson', 'alicejohnson@example.com', 'password789', 'Hello, my name is Alice.', (SELECT readfile('./images/mouflon.jpg'))),
    ('David', 'Brown', 'davidbrown', 'davidbrown@example.com', 'passwordabc', 'Hello, my name is David.', NULL),
    ('Alice', 'Smith', 'alicesmith', 'alicesmith@example.com', 'passworddef', 'Nice to meet you!', NULL),
    ('Michael', 'Johnson', 'michaeljohnson', 'michaeljohnson@example.com', 'passwordghi', 'I love sports!', NULL),
    ('Emily', 'Brown', 'emilybrown', 'emilybrown@example.com', 'passwordjkl', 'Exploring the world one step at a time.', NULL),
    ('David', 'Miller', 'davidmiller', 'davidmiller@example.com', 'passwordmno', 'Coffee enthusiast and book lover.', NULL),
    ('Sophia', 'Davis', 'sophiadavis', 'sophiadavis@example.com', 'passwordpqr', 'Passionate about art and creativity.', NULL),
    ('Daniel', 'Wilson', 'danielwilson', 'danielwilson@example.com', 'passwordstu', 'Nature lover and outdoor enthusiast.', NULL),
    ('Olivia', 'Anderson', 'oliviaanderson', 'oliviaanderson@example.com', 'passwordvwx', 'Foodie and cooking enthusiast.', NULL),
    ('James', 'Thomas', 'jamesthomas', 'jamesthomas@example.com', 'passwordyza', 'Music is life!', NULL),
    ('Emma', 'Martinez', 'emmamartinez', 'emmamartinez@example.com', 'passwordbcd', 'Adventure awaits!', NULL),
    ('Sarah', 'Wilson', 'sarahwilson', 'sarahwilson@example.com', 'passwordefg', 'Nature lover and photography enthusiast.', NULL),
    ('Christopher', 'Taylor', 'christophertaylor', 'christophertaylor@example.com', 'passwordhij', 'Tech geek and coding enthusiast.', NULL),
    ('Elizabeth', 'Clark', 'elizabethclark', 'elizabethclark@example.com', 'passwordklm', 'Bookworm and literature lover.', NULL),
    ('Matthew', 'Rodriguez', 'matthewrodriguez', 'matthewrodriguez@example.com', 'passwordnop', 'Fitness and healthy lifestyle advocate.', NULL),
    ('Ava', 'Hernandez', 'avahernandez', 'avahernandez@example.com', 'passwordqrs', 'Passionate about fashion and style.', NULL),
    ('William', 'Garcia', 'williamgarcia', 'williamgarcia@example.com', 'passwordtuv', 'Sports fanatic and adrenaline junkie.', NULL),
    ('Mia', 'Lopez', 'mialopez', 'mialopez@example.com', 'passwordwxy', 'Dreamer and aspiring writer.', NULL),
    ('Benjamin', 'Lee', 'benjaminlee', 'benjaminlee@example.com', 'passwordzab', 'Gamer and technology enthusiast.', NULL),
    ('Ella', 'Sanchez', 'ellasanchez', 'ellasanchez@example.com', 'passwordcde', 'Art lover and museum explorer.', NULL),
    ('Alexander', 'Rivera', 'alexanderrivera', 'alexanderrivera@example.com', 'passwordfgh', 'Coffee addict and barista.', NULL);


-- HINT: This will only work if you have the images locally ;)
INSERT INTO Event (name, description, owner_user_id, category, location, date, image, visibility, promoted)
VALUES
    ('Sunset Beach Party', 'Good vibes only', 1, 'Music', 'Costa Rica', '2023-07-28 19:00', (SELECT readfile('./images/celebration.jpg')), 'Public', false),
    ('Rock Concert', 'The best event for Rock fans', 2, 'Music', 'City Hall Texas', '2023-08-05 18:30', (SELECT readfile('./images/concert.jpg')), 'Public', false),
    ('Beer', 'Standard Guys getting drunk event', 3, 'Music', 'Moms Condo', '2023-07-03 20:00', (SELECT readfile('./images/beer.jpg')), 'Public', false),
    ('Sisters Wedding', 'Not jealous at all.', 4, 'Arts and Culture', 'Big Castle', '2023-07-11 10:00', (SELECT readfile('./images/wedding.jpg')), 'Public', false),
    ('Yoga in the Park', 'Relax and rejuvenate with outdoor yoga', 1, 'Sports and Fitness', 'Central Park', '2023-07-15 09:00', (SELECT readfile('./images/yoga.jpg')), 'Public', false),
    ('Tech Conference', 'Stay up to date with the latest technology trends', 1, 'Workshops and Education', 'Convention Center', '2023-08-10 10:00', (SELECT readfile('./images/conference.jpg')), 'Public', false),
    ('Food Festival', 'A feast for food lovers', 1, 'Food and Drink', 'Downtown Square', '2023-07-20 16:00', (SELECT readfile('./images/foodfestival.jpg')), 'Public', false),
    ('Art Exhibition', 'Discover the beauty of contemporary art', 3, 'Arts and Culture', 'Art Gallery', '2023-07-08 14:00', NULL, 'Public', false),
    ('Charity Run', 'Run for a cause and make a difference', 3, 'Sports and Fitness', 'City Park', '2023-07-22 07:30', (SELECT readfile('./images/charityrun.jpg')), 'Public', false),
    ('Movie Night', 'Enjoy a classic movie under the stars', 1, 'Entertainment', 'Rooftop Cinema', '2023-07-16 20:30', NULL, 'Public', false),
    ('Fashion Show', 'Experience the latest fashion trends', 2, 'Arts and Culture', 'Fashion Arena', '2023-07-25 19:00', NULL, 'Public', false),
    ('Book Signing', 'Meet your favorite author and get a signed copy', 5, 'Workshops and Education', 'Bookstore', '2023-08-02 17:00', NULL, 'Public', false),
    ('Fitness Bootcamp', 'Challenge yourself with intense workout sessions', 1, 'Sports and Fitness', 'Fitness Center', '2023-07-18 06:00', NULL, 'Public',false),
    ('Gaming Tournament', 'Compete against the best gamers in town', 4, 'Entertainment', 'Gaming Arena', '2023-07-29 12:00', NULL, 'Public', false),
    ('Wine Tasting', 'Discover the flavors of exquisite wines', 1, 'Food and Drink', 'Winery', '2023-08-01 18:00', NULL, 'Public', false),
    ('Car Show', 'Admire vintage and luxury cars', 5, 'Automotive', 'Exhibition Center', '2023-07-12 11:00', NULL, 'Public', false),
    ('Comedy Night', 'Laugh out loud with hilarious stand-up comedians', 1, 'Entertainment', 'Comedy Club', '2023-07-23 21:00', NULL, 'Public', false),
    ('Dance Workshop', 'Learn new dance moves from professional dancers', 4, 'Workshops and Education', 'Dance Studio', '2023-07-19 15:00', NULL, 'Public', false),
    ('Science Exhibition', 'Explore the wonders of science through interactive exhibits', 8, 'Workshops and Education', 'Science Museum', '2023-07-30 10:00', NULL, 'Public', false),
    ('Live Music Performance', 'Enjoy live music by talented local artists', 5, 'Music', 'Music Venue', '2023-07-14 19:30', NULL, 'Public', false),
    ('Cooking Class', 'Learn to cook delicious dishes from expert chefs', 3, 'Food and Drink', 'Cooking School', '2023-07-17 16:00', NULL, 'Public', false),
    ('Fitness Seminar', 'Get insights from fitness experts and improve your workouts', 1, 'Sports and Fitness', 'Community Center', '2023-07-26 11:00', NULL, 'Public', false),
    ('Photography Workshop', 'Enhance your photography skills with professional guidance', 5, 'Workshops and Education', 'Photography Studio', '2023-08-03 14:00', NULL, 'Public', false),
    ('Dog Show', 'Witness adorable dogs showcasing their talents', 3, 'Pets and Animals', 'Dog Park', '2023-07-31 13:00', NULL, 'Public',false);



    INSERT INTO FriendRequest (sender, receiver, status)
    VALUES
        (2, 1, 'PENDING'),
        (3, 1, 'PENDING'),
        (4, 1, 'PENDING'),
        (5, 1, 'PENDING'),
        (6, 1, 'PENDING'),
        (7, 1, 'PENDING'),
        (8, 1, 'PENDING'),
        (9, 1, 'PENDING'),
        (1, 5, 'PENDING'),
        (1, 4, 'PENDING'),
        (1, 6, 'PENDING');



INSERT INTO Invitee (event_id, user_id) VALUES (1, 2);
INSERT INTO Invitee (event_id, user_id) VALUES (2, 1);

INSERT INTO Attendee (event_id, user_id) VALUES (1, 1);
INSERT INTO Attendee (event_id, user_id) VALUES (2, 2);

INSERT INTO Rating (event_id, user_id, comment, rating) VALUES (1, 2, 'Great party!', 5);
INSERT INTO Rating (event_id, user_id, comment, rating) VALUES (2, 1, 'Excellent conference!', 4);


INSERT INTO Rating (rating, comment, date, event_id, user_id)
VALUES
  (4, 'The event was fantastic! The organizers did an amazing job.', '2023-05-21', 1, 1),
  (3, 'I enjoyed the event, but I wish there were more interactive activities.', '2023-05-21', 2, 2),
  (5, 'Wow! This event exceeded my expectations. I had a blast!', '2023-05-21', 3, 3),
  (2, 'Unfortunately, the event fell short of my expectations. There were long queues and disorganized arrangements.', '2023-05-21', 4, 4),
  (1, 'I was not impressed with the event. The performances were lackluster.', '2023-05-21', 1, 2),
  (4, 'The event had a great atmosphere, and the food was delicious!', '2023-05-21', 2, 3),
  (3, 'The event had some interesting sessions, but overall, it could have been better.', '2023-05-21', 3, 4),
  (5, 'I had an amazing time at the event. The organizers really went above and beyond.', '2023-05-21', 4, 1),
  (2, 'The event had potential, but there were technical issues that affected the experience.', '2023-05-21', 1, 3),
  (4, 'I loved the event! The guest speakers were inspiring, and I made valuable connections.', '2023-05-21', 2, 4);
