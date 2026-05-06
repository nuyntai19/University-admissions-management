package vn.edu.sgu.tuyensinhweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.sgu.tuyensinhweb.model.BangQuyDoi;
import vn.edu.sgu.tuyensinhweb.repository.BangQuyDoiRepository;

import java.util.List;

@RestController
public class TempController {

    @Autowired
    private BangQuyDoiRepository bqdRepo;

    @GetMapping("/temp-bqd")
    public List<BangQuyDoi> getAllBqd() {
        return bqdRepo.findAll();
    }
}
