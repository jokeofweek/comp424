package main

import (
	"log"
	"os/exec"
	"sync"
	"time"
)

func runAndWaitFor(group *sync.WaitGroup, cmd *exec.Cmd) {
	println("Starting command.")
	err := cmd.Start()
	if err != nil {
		log.Fatal(err)
	} else {
		err = cmd.Wait()
		println("Command done.")
	}
	group.Done()
}

func runServer(group *sync.WaitGroup) {
	cmd := exec.Command("ant", "bootstrap-server")
	runAndWaitFor(group, cmd)
}

func runClient(group *sync.WaitGroup) {
	cmd := exec.Command("ant", "bootstrap-client")
	runAndWaitFor(group, cmd)
}

func main() {
    var group sync.WaitGroup
    group.Add(1)
	go runServer(&group)

    time.Sleep(1000 * time.Millisecond)

	for i := 0; i < 4; i++ {
		group.Add(1)
		go runClient(&group)
	}
	group.Wait()
	println("Done")
}