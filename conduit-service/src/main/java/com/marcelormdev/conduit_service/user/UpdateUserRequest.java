package com.marcelormdev.conduit_service.user;

public record UpdateUserRequest(Params user) {

    public UpdateUserRequest(String email, String password, String username, String bio, String image) {
        this(buildParams(email, password, username, bio, image));
    }

    private static Params buildParams(String email, String password, String username, String bio, String image) {
        Params params = new Params();
        params.setEmail(email);
        params.setPassword(password);
        params.setUsername(username);
        if (bio != null)
            params.setBio(bio);
        if (image != null)
            params.setImage(image);
        return params;
    }

    public static class Params {
        private String email;
        private String password;
        private String username;
        private String bio;
        private String image;
        private boolean hasBio;
        private boolean hasImage;

        public Params() {
        }

        public String email() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String password() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String username() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String bio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
            this.hasBio = true;
        }

        public String image() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
            this.hasImage = true;
        }

        public boolean hasBio() {
            return hasBio;
        }

        public boolean hasImage() {
            return hasImage;
        }
    }
}
