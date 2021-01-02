# Dovekeeper

## Server for Message File Exchange

### Advantage of the common Mail protocols
MFE is designed to replace common Emails with a new approach.
Email was a child of his time.
To secure the concept of Mail with attachments and Header etc. it needs a new approach.
MFE is fully encrypted with direct build-in PGP by design. To divert old Emails from this format the Address got a slight 
different design.
The Server can Store the Keys, but can hold only the public without further encryption.




### How it Works
MFE accepts JSON with File transfer via HTTPS:7331.
All Files are encrypted with PGP based on the public key of the recipient of the massage.
The Recipient is mentions with these pattern:
```url.com//someone```

The url can be any common url. The name (someone it this example) should only `accept a-z0-9.-_`

The Message has to be introduced wit a POST Request which must include the following pattern:
```
{
    sender: "somewehere.org/someone-else"
    recipient: "url.com/someone"
    mailid: "somestring_including_timecode"
}
```
POST/Multipart to `url.com:7331/in`

Within the file there are an encrypted file with the Email text named `msg` and if needed a file called
`fmsg` which contains HTML Text if wanted.
All other Files are also encrypted.


The recipient MFE Server will now ask the sending Server if it was really send by the sender by sending the `mailid`.
GET `somewhere.org/proof/somestring_including_timecode`

The Sender should now confirm with JSON Content:
```
{
    sender: "somewehere.org/someone-else",
    recipient: "url.com/someone",
    mailid: "somestring_including_timecode",
    msg_sha512: "sha512stuffhere",
    fmsg_sha512: "only appears if html formated message was sended",
    attachments: [
    "sone_sha512_stuff_for each other file", ....
    ],
    MFE_comp: true
}
```
The recipient can check every file for the hash
`MFE_comp` is very important. With the boolean the sender will agree, that the original Dovekeeper Software is used und completly untapperd.
If it is TRUE and the Software is tampered or seized the owner can be sued. Also the Sender domain will be blacklisted and further 
Dovekeeper does not accept any Message send by this Domain.

The files will be stored by the Dovekeeper in the following pattern:

```
root/
    users/
        username/
            user-pw-sha512/
                conf/keys/
                        pub.key
                        enc_priv_key.key
                mails/
                    inbox/
                        mail-ID/
                            msg
                            fmsg
                            file1sha512
                            file2sha512
                            meta
                        mail-ID/
                            msg
                            fmsg
                            meta
                    random_folder_name/
                        mail-ID/
                            msg
                            fmsg
                            meta
                    outbox/
                          mail-ID/
                            msg
                            fmsg
                            meta                
```

## API

`@RequestMapping(value = "/in", method = RequestMethod.POST)`

* `@RequestParam("sender") String sender` sender of incoming Mail
* `@RequestParam("recipient") String recipient` the recipient of the incoming Mail
* `@RequestParam("mailId") String mailId` the mailId given by sending Server
* `@RequestParam("attachments") List<MultipartFile> files` the files

There must be an `msg` file attached otherwise the server will not accept it.
An encrypted HTML-Text File is optional (must be named `fmsg`)
All other files are optional and threaten as attachment.


`@RequestMapping(value = "/in/probe", method = RequestMethod.POST)`

* `public ResponseEntity probeSendMail(@RequestBody Probe probeModel)` the JSON to Probe if the Mail was send by the sender.
To ensure that the content was untouched, the client of the recipient can probe it.
The Strings in `attachment` are the SHA-512 of the content of each file, without snitching what file is meant.

The JSON example:
```
{
    mailId : "String",
    sender : "String",
    recipient : "String",
    attachments : ["String"]
}
```


    @RequestMapping(value = "/inbox/create", method = RequestMethod.POST)
    public String createUser(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/delete", method = RequestMethod.POST)
    public String deleteUser(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/info", method = RequestMethod.POST)
    public String fetchInboxInformations(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/in/pubkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/privkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user,
                                   @RequestParam("hash") String pwhash,
                                   @RequestParam("pub") MultipartFile pubKey,
                                   @RequestParam("priv") MultipartFile privKey) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)
    public String fetchInboxList(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/file", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId, @RequestParam("fileId") String fileId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/remove", method = RequestMethod.POST)
    public String removeMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/move", method = RequestMethod.POST)
    public String moveMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public String sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("recipients") List<String> recipients
    ) {
        return "JSON";
    }