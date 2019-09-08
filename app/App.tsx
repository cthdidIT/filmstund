import * as React from "react";
import {
  Image,
  ScrollView,
  StyleProp,
  Text,
  TouchableOpacity,
  View,
  ViewStyle
} from "react-native";

import {
  createAppContainer,
  ScreenProps,
  StackActions
} from "react-navigation";
import { createStackNavigator } from "react-navigation-stack";
// @ts-ignore
import { createBottomTabNavigator } from "react-navigation-tabs";

const padding = 11;

const moviePoster =
  "https://catalog.cinema-api.com/images/ncg-images/e1cf3dd601ec4f23b4231f901f7b3c29.jpg?version=11D63C967B3576D4D5DBDE2A3ACFA3AB&width=240";

const ShowingListItem: React.FC<{ onPressShowTicket: () => void }> = ({
  onPressShowTicket
}) => (
  <View
    style={{
      padding,
      backgroundColor: "white",
      borderRadius: 5,
      marginBottom: 8,
      flexDirection: "row"
    }}
  >
    <Image source={{ uri: moviePoster, height: 199, width: 134 }} />
    <View style={{ paddingHorizontal: padding, flex: 1 }}>
      <View style={{ flex: 1 }}>
        <Text style={{ fontSize: 18, fontWeight: "500" }}>
          Once upon a time in Hollywood
        </Text>
        <Text>11 jan 19:30</Text>
        <Text>Bergakungen, salong 13</Text>
      </View>
      <View style={{ alignItems: "flex-start" }}>
        <TouchableOpacity onPress={onPressShowTicket}>
          <View
            style={{
              backgroundColor: "#d0021b",
              borderRadius: 5,
              padding: 13
            }}
          >
            <Text
              style={{
                color: "white",
                fontSize: 14,
                fontWeight: "500"
              }}
            >
              Visa biljett
            </Text>
          </View>
        </TouchableOpacity>
      </View>
    </View>
    <View>
      <Text>{">"}</Text>
    </View>
  </View>
);

const ShowingListItemContainer: React.FC<{ style?: StyleProp<ViewStyle> }> = ({
  children,
  style = {}
}) => <View style={[{ padding }, style]}>{children}</View>;

const RedHeader: React.FC = ({ children }) => (
  <View>
    <Text
      style={{
        color: "#4a4a4a",
        fontSize: 18,
        fontWeight: "500",
        paddingBottom: 5
      }}
    >
      {children}
    </Text>
    <View style={{ backgroundColor: "#d0021b", flex: 1, height: 4 }} />
  </View>
);

const aztecCode =
  "data:image/jpeg;base64,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";

const TicketScreen: React.FC = () => {
  return (
    <View>
      <Image source={{ uri: aztecCode, width: 128, height: 128 }} />
      <Text>Welcome to the jungle</Text>
    </View>
  );
};

const TodayScreen: React.FC<ScreenProps> = ({ navigation }) => {
  const onPress = () =>
    navigation.dispatch(
      StackActions.push({
        routeName: "Ticket",
        params: {
          showingId: "showingId"
        }
      })
    );

  return (
    <ScrollView>
      <ShowingListItemContainer
        style={{ paddingVertical: 25, backgroundColor: "black" }}
      >
        <ShowingListItem onPressShowTicket={onPress} />
      </ShowingListItemContainer>
      <ShowingListItemContainer>
        <RedHeader>Mina kommande besök</RedHeader>
        <ShowingListItem onPressShowTicket={onPress} />
        <ShowingListItem onPressShowTicket={onPress} />
        <ShowingListItem onPressShowTicket={onPress} />
        <ShowingListItem onPressShowTicket={onPress} />
        <ShowingListItem onPressShowTicket={onPress} />
      </ShowingListItemContainer>
    </ScrollView>
  );
};

const MoviesScreen: React.FC = () => {
  return <Text>Movies</Text>;
};

const ShowingsScreen: React.FC = () => {
  return <Text>Showings</Text>;
};

const ProfileScreen: React.FC = () => {
  return <Text>Profile</Text>;
};

const TodayStack = createStackNavigator({
  Today: {
    screen: TodayScreen,
    navigationOptions: options => ({
      title: "Today"
    })
  },
  Ticket: {
    screen: TicketScreen
  }
});

const TabNavigator = createBottomTabNavigator(
  {
    Today: TodayStack,
    Movies: MoviesScreen,
    Showings: ShowingsScreen,
    Account: ProfileScreen
  },
  {
    tabBarOptions: {
      activeTintColor: "#fff",
      inactiveTintColor: "#e6b8be",
      style: {
        color: "white",
        backgroundColor: "#d0021b"
      }
    }
  }
);

export default createAppContainer(TabNavigator);
